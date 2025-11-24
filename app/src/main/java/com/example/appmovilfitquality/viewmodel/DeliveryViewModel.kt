package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.repository.OrderRepository
import com.example.appmovilfitquality.data.repository.OrderRepository.OrderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class DeliveryUiState(
    val orders: List<OrderEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastMessage: String? = null
)


class DeliveryViewModel(
    private val repo: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeliveryUiState(isLoading = true))
    val uiState: StateFlow<DeliveryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, lastMessage = null)
            try {
                // Llama a OrderRepository (que llama al endpoint GET /ventas)
                val list = repo.getOrdersForDelivery()
                _uiState.value = DeliveryUiState(orders = list, isLoading = false)
            } catch (e: HttpException) {
                _uiState.value = DeliveryUiState(isLoading = false, error = "Error ${e.code()}: No se pudieron cargar los pedidos.")
            } catch (e: IOException) {
                _uiState.value = DeliveryUiState(isLoading = false, error = "Error de red: Imposible conectar con el microservicio.")
            } catch (e: Exception) {
                _uiState.value = DeliveryUiState(isLoading = false, error = "Error desconocido: ${e.message}")
            }
        }
    }


    fun submitProof(orderId: Int, proofUri: String) {
        viewModelScope.launch {
            try {

                repo.saveDeliveryProof(orderId, proofUri)


                refresh()

                _uiState.value = _uiState.value.copy(
                    lastMessage = "Comprobante guardado y pedido marcado como entregado."
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(error = "Error ${e.code()}: No se pudo guardar el comprobante.")
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(error = "Error de red al subir comprobante.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "No se pudo guardar el comprobante.")
            }
        }
    }
}