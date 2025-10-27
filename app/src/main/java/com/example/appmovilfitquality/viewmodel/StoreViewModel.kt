package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.repository.ProductRepository
import com.example.appmovilfitquality.data.local.ProductEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StoreUiState(
    val products: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class StoreViewModel(private val repo: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            try {
                repo.getAllProducts().collect { list ->
                    _uiState.value = StoreUiState(products = list, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = StoreUiState(error = "Error al cargar productos.")
            }
        }
    }

    fun addProduct(product: ProductEntity) {
        viewModelScope.launch { repo.addProduct(product) }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch { repo.deleteProduct(product) }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch { repo.updateProduct(product) }
    }
}