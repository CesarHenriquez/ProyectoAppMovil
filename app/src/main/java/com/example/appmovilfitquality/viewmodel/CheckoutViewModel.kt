package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.local.OrderEntity
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.data.repository.OrderRepository
import com.example.appmovilfitquality.data.repository.ProductRepository // ⬅️ Importación de ProductRepository
import com.example.appmovilfitquality.domain.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val isPlacing: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class CheckoutViewModel(
    private val orderRepo: OrderRepository,
    private val authRepo: AuthRepository,
    private val session: SessionManager,
    private val productRepo: ProductRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(CheckoutUiState())
    val ui: StateFlow<CheckoutUiState> = _ui.asStateFlow()

    fun reset() {
        _ui.value = CheckoutUiState()
    }

    // Procesa la compra: valida stock, lo reduce, y luego persiste la orden.

    fun placeOrderFromCart(items: List<CartItem>, shippingAddress: String) {
        viewModelScope.launch {
            if (items.isEmpty()) {
                _ui.value = CheckoutUiState(error = "El carrito está vacío.")
                return@launch
            }
            if (shippingAddress.isBlank()) {
                _ui.value = CheckoutUiState(error = "Debe ingresar una dirección de envío.")
                return@launch
            }

            _ui.value = CheckoutUiState(isPlacing = true)

            try {
                val email = session.emailFlow.first()
                if (email.isNullOrBlank()) {
                    _ui.value = CheckoutUiState(error = "No hay sesión activa.")
                    return@launch
                }

                val user = authRepo.getUserByEmail(email)
                if (user == null) {
                    _ui.value = CheckoutUiState(error = "Usuario no encontrado.")
                    return@launch
                }

                //  VALIDACIÓN Y REDUCCIÓN DE STOCK
                // Si falla, el repositorio retorna false y no se realiza la compra
                val stockReduced = productRepo.tryReduceStock(items)
                if (!stockReduced) {
                    _ui.value = CheckoutUiState(error = "Stock insuficiente. Por favor, revise las cantidades en su carrito.")
                    // Se retorna para detener la transacción
                    return@launch
                }

                //  Si el stock fue reducido con éxito, procedemos a guardar la Orden.
                val total = items.sumOf { it.subtotal }


                val orderEntity = OrderEntity(
                    customerName  = user.name,
                    customerEmail = user.email,
                    customerPhone = user.phone,
                    shippingAddress = shippingAddress.trim(),
                    totalCLP = total
                )

                orderRepo.placeOrder(orderEntity, items)
                _ui.value = CheckoutUiState(success = true)

            } catch (e: Exception) {
                _ui.value = CheckoutUiState(error = "No se pudo completar la compra: Error de conexión o base de datos.")
            }
        }
    }
}