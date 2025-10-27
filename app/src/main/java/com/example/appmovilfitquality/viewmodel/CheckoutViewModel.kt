package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.local.OrderEntity
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.data.repository.DeliveryRepository
import com.example.appmovilfitquality.data.localstore.SessionManager
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
    private val deliveryRepo: DeliveryRepository,
    private val authRepo: AuthRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _ui = MutableStateFlow(CheckoutUiState())
    val ui: StateFlow<CheckoutUiState> = _ui.asStateFlow()

    fun reset() {
        _ui.value = CheckoutUiState()
    }

    // Crea OrderEntity desde el carrito + dirección y lo guarda.
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

                val summary = items.joinToString(", ") { "${it.product.name} x${it.quantity}" }
                val total = items.sumOf { it.subtotal }

                val order = OrderEntity(
                    customerName  = user.name,
                    customerEmail = user.email,
                    customerPhone = user.phone,
                    shippingAddress = shippingAddress.trim(),
                    productSummary = summary,
                    totalCLP = total
                )

                deliveryRepo.placeOrder(order)
                _ui.value = CheckoutUiState(success = true)

            } catch (e: Exception) {
                _ui.value = CheckoutUiState(error = "No se pudo completar la compra.")
            }
        }
    }
}