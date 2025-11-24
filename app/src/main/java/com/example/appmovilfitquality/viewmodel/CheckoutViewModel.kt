package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.data.repository.OrderRepository
import com.example.appmovilfitquality.data.repository.ProductRepository // Mantenemos la inyección por si hay validación local
import com.example.appmovilfitquality.domain.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class CheckoutUiState(
    val isPlacing: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class CheckoutViewModel(
    private val orderRepo: OrderRepository,
    private val authRepo: AuthRepository,
    private val sessionManager: SessionManager,
    private val productRepo: ProductRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(CheckoutUiState())
    val ui: StateFlow<CheckoutUiState> = _ui.asStateFlow()

    fun reset() {
        _ui.value = CheckoutUiState()
    }


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

                val email = sessionManager.emailFlow.first()
                if (email.isNullOrBlank()) {
                    _ui.value = CheckoutUiState(error = "No hay sesión activa para realizar la compra.")
                    return@launch
                }




                orderRepo.placeOrder(email, shippingAddress, items)

                _ui.value = CheckoutUiState(success = true)

            } catch (e: HttpException) {

                _ui.value = CheckoutUiState(error = "Error ${e.code()}: Compra fallida. Revise el stock o su dirección.")
            } catch (e: IOException) {
                _ui.value = CheckoutUiState(error = "Error de red: Imposible conectar con el microservicio de ventas.")
            } catch (e: Exception) {
                _ui.value = CheckoutUiState(error = "No se pudo completar la compra: ${e.message}")
            }
        }
    }
}