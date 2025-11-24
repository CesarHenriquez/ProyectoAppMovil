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
            try {

                val userId = authRepo.getCurrentUserId()


                if (userId == null || userId == 0L) {
                    _uiState.value = HistoryUiState(
                        isLoading = false,
                        error = "ID de usuario no disponible localmente. Intente cerrar y abrir sesión nuevamente."
                    )
                    return@launch
                }

                val orders: List<Order> = if (isAdminView) {
                    repo.getAllOrders()
                } else {

                    repo.getOrdersByCustomerId(userId)
                }


                _uiState.value = HistoryUiState(orders = orders, isLoading = false)

            } catch (e: HttpException) {
                _uiState.value = HistoryUiState(
                    isLoading = false,
                    error = "Error ${e.code()}: No se pudo cargar el historial. Asegúrese de tener compras registradas."
                )
            } catch (e: IOException) {
                _uiState.value = HistoryUiState(
                    isLoading = false,
                    error = "Error de red: Imposible conectar con el microservicio (8023)."
                )
            } catch (e: Exception) {
                _uiState.value = HistoryUiState(
                    isLoading = false,
                    error = "Error desconocido al cargar historial: ${e.message}"
                )
            }
        }
    }
}