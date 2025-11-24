package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.OrderRepository
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.domain.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class HistoryUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HistoryViewModel(
    private val repo: OrderRepository,
    private val session: SessionManager,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun loadHistory(isAdminView: Boolean) {
        viewModelScope.launch {
            _uiState.value = HistoryUiState(isLoading = true)

            // ⬇️ LOG INICIAL ⬇️
            android.util.Log.d("DEBUG_HISTORY", "=== INICIANDO CARGA DE HISTORIAL ===")
            android.util.Log.d("DEBUG_HISTORY", "Modo Admin: $isAdminView")

            try {
                // 1. Obtener ID
                android.util.Log.d("DEBUG_HISTORY", "Solicitando ID de usuario...")
                val userId = authRepo.getCurrentUserId()
                android.util.Log.d("DEBUG_HISTORY", "ID obtenido: $userId")

                if (userId == null || userId == 0L) {
                    android.util.Log.e("DEBUG_HISTORY", "❌ Error: ID de usuario nulo o cero.")
                    _uiState.value = HistoryUiState(
                        isLoading = false,
                        error = "ID de usuario no disponible localmente. Intente cerrar y abrir sesión nuevamente."
                    )
                    return@launch
                }

                // 2. Llamar al repositorio
                android.util.Log.d("DEBUG_HISTORY", "Llamando a la API (Repo)...")

                val orders: List<Order> = if (isAdminView) {
                    repo.getAllOrders()
                } else {
                    repo.getOrdersByCustomerId(userId)
                }

                // 3. Loguear resultados
                android.util.Log.d("DEBUG_HISTORY", "✅ Respuesta recibida. Cantidad de órdenes: ${orders.size}")
                orders.forEach { o ->
                    android.util.Log.d("DEBUG_HISTORY", "ORDEN -> ID: ${o.id} | Total: ${o.totalCLP} | Fecha(long): ${o.timestamp}")
                }

                _uiState.value = HistoryUiState(orders = orders, isLoading = false)

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("DEBUG_HISTORY", "❌ Error HTTP ${e.code()}: $errorBody")

                _uiState.value = HistoryUiState(
                    isLoading = false,
                    error = "Error ${e.code()}: No se pudo cargar el historial."
                )
            } catch (e: IOException) {
                android.util.Log.e("DEBUG_HISTORY", "❌ Error de Red: ${e.message}")
                _uiState.value = HistoryUiState(
                    isLoading = false,
                    error = "Error de red: Imposible conectar con el microservicio (8023)."
                )
            } catch (e: Exception) {
                // Este bloque atrapará errores de parseo (como el de la fecha)
                android.util.Log.e("DEBUG_HISTORY", "❌ Error Desconocido (Posiblemente mapeo): ${e.message}")
                e.printStackTrace()

                _uiState.value = HistoryUiState(
                    isLoading = false,
                    error = "Error desconocido al cargar historial: ${e.message}"
                )
            }
        }
    }
}