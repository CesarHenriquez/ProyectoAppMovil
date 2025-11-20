package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.local.OrderEntity
import com.example.appmovilfitquality.data.repository.OrderRepository // ⬅️ Usamos OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DeliveryUiState(
    val orders: List<OrderEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastMessage: String? = null
)

class DeliveryViewModel(
    private val repo: OrderRepository // Inyección del OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeliveryUiState(isLoading = true))
    val uiState: StateFlow<DeliveryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    //Carga todas las OrderEntity (útil para el Delivery que solo ve las entregas).

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, lastMessage = null)
            try {

                val list = repo.getOrdersForDelivery()
                _uiState.value = DeliveryUiState(orders = list, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = DeliveryUiState(isLoading = false, error = "No fue posible cargar los pedidos.")
            }
        }
    }

    // guardar comprobante y recargar lista
    fun submitProof(orderId: Int, proofUri: String) {
        viewModelScope.launch {
            try {
                repo.saveDeliveryProof(orderId, proofUri)
                val list = repo.getOrdersForDelivery() // Recarga
                _uiState.value = DeliveryUiState(
                    orders = list,
                    isLoading = false,
                    lastMessage = "Comprobante guardado."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "No se pudo guardar el comprobante.")
            }
        }
    }
}