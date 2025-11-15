package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.OrderRepository
import com.example.appmovilfitquality.domain.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class HistoryUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HistoryViewModel(
    private val repo: OrderRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    // Carga el historial basado en el rol de la sesión activa
    fun loadHistory(isAdminView: Boolean) {
        viewModelScope.launch {
            _uiState.value = HistoryUiState(isLoading = true)
            try {
                val orders: List<Order> = if (isAdminView) {
                    // Carga todas las ventas (Admin)
                    repo.getAllOrders()
                } else {
                    // Carga las compras de la sesión (Cliente)
                    val email = session.emailFlow.first()
                    if (email.isNullOrBlank()) throw IllegalStateException("No hay sesión activa para cargar el historial.")
                    repo.getOrdersByCustomer(email)
                }
                _uiState.value = HistoryUiState(orders = orders, isLoading = false)

            } catch (e: Exception) {
                _uiState.value = HistoryUiState(
                    isLoading = false,
                    error = "Error al cargar historial: ${e.message}"
                )
            }
        }
    }
}