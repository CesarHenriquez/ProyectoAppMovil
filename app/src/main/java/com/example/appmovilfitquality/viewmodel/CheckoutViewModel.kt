package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.data.repository.OrderRepository
import com.example.appmovilfitquality.data.repository.ProductRepository
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
            // LOG INICIAL
            android.util.Log.d("DEBUG_CHECKOUT", "=== INICIANDO PROCESO DE COMPRA ===")

            if (items.isEmpty()) {
                android.util.Log.e("DEBUG_CHECKOUT", "Error: El carrito está vacío.")
                _ui.value = CheckoutUiState(error = "El carrito está vacío.")
                return@launch
            }
            if (shippingAddress.isBlank()) {
                android.util.Log.e("DEBUG_CHECKOUT", "Error: Dirección vacía.")
                _ui.value = CheckoutUiState(error = "Debe ingresar una dirección de envío.")
                return@launch
            }

            _ui.value = CheckoutUiState(isPlacing = true)

            try {
                // 1. Obtener email de la sesión
                android.util.Log.d("DEBUG_CHECKOUT", "Intentando leer email de sesión...")
                val email = sessionManager.emailFlow.first()
                android.util.Log.d("DEBUG_CHECKOUT", "Email obtenido: $email")

                if (email.isNullOrBlank()) {
                    android.util.Log.e("DEBUG_CHECKOUT", "Error: No hay email en sesión.")
                    _ui.value = CheckoutUiState(error = "No hay sesión activa para realizar la compra.")
                    return@launch
                }

                // 2. Llamada al repositorio
                android.util.Log.d("DEBUG_CHECKOUT", "Llamando a orderRepo.placeOrder...")
                android.util.Log.d("DEBUG_CHECKOUT", "Datos: Email=$email, Dirección=$shippingAddress, CantidadItems=${items.size}")

                // Esta llamada hace la petición a la API (POST /api/ventas)
                orderRepo.placeOrder(email, shippingAddress, items)

                android.util.Log.d("DEBUG_CHECKOUT", "✅ Respuesta exitosa del servidor. Compra creada.")
                _ui.value = CheckoutUiState(success = true)

            } catch (e: HttpException) {
                // ERROR DEL SERVIDOR (Spring devolvió 400, 500, etc.)
                val errorBody = e.response()?.errorBody()?.string()
                val errorCode = e.code()

                android.util.Log.e("DEBUG_CHECKOUT", "❌ Error HTTP $errorCode del servidor.")
                android.util.Log.e("DEBUG_CHECKOUT", "❌ Mensaje del servidor: $errorBody")

                _ui.value = CheckoutUiState(error = "Error $errorCode: $errorBody")

            } catch (e: IOException) {
                // ERROR DE CONEXIÓN (No salió del teléfono o servidor apagado)
                android.util.Log.e("DEBUG_CHECKOUT", "❌ Error de Red/Conexión: ${e.message}")
                _ui.value = CheckoutUiState(error = "Error de red: Imposible conectar con el microservicio de ventas.")

            } catch (e: IllegalStateException) {
                // ERROR DE LÓGICA INTERNA (ej: ID de usuario no encontrado en Repo)
                android.util.Log.e("DEBUG_CHECKOUT", "❌ Error de Estado (Posible ID nulo): ${e.message}")
                _ui.value = CheckoutUiState(error = e.message ?: "Error de estado desconocido")

            } catch (e: Exception) {
                // CUALQUIER OTRO ERROR
                android.util.Log.e("DEBUG_CHECKOUT", "❌ Error desconocido: ${e.message}")
                e.printStackTrace() // Imprime toda la traza en el logcat
                _ui.value = CheckoutUiState(error = "No se pudo completar la compra: ${e.message}")
            }
        }
    }
}