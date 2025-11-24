package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.repository.ProductRepository
import com.example.appmovilfitquality.data.repository.ProductRepository.ProductEntity // ⬅️ IMPORTACIÓN DEL MODELO INTERMEDIO
import com.example.appmovilfitquality.domain.model.Product // ⬅️ IMPORTAMOS EL MODELO DE DOMINIO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


data class StoreUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)


class StoreViewModel(private val repo: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState(isLoading = true))
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()


    private fun ProductEntity.toDomain() = Product(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        stock = this.stock,

        imageUri = this.imageUri,
        imageResourceName = this.imageResourceName ?: ""
    )


    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {

                repo.getAllProducts().collect { listEntity ->

                    val listDomain = listEntity.map { it.toDomain() }
                    _uiState.value = StoreUiState(products = listDomain, isLoading = false)
                }
            } catch (e: HttpException) {
                _uiState.value = StoreUiState(error = "Error HTTP: ${e.code()}. No se pudieron cargar los productos.")
            } catch (e: IOException) {
                _uiState.value = StoreUiState(error = "Error de red. Verifique la conexión al microservicio.")
            } catch (e: Exception) {
                _uiState.value = StoreUiState(error = "Error al cargar productos: ${e.message}")
            }
        }
    }



    fun addProduct(productEntity: ProductEntity) {
        viewModelScope.launch {
            runCatching { repo.addProduct(productEntity) }
                .onFailure { handleApiError(it, "Error al agregar el producto.") }
                .onSuccess { loadProducts() }
        }
    }

    fun deleteProduct(productEntity: ProductEntity) {
        viewModelScope.launch {
            runCatching { repo.deleteProduct(productEntity) }
                .onFailure { handleApiError(it, "Error al eliminar el producto.") }
                .onSuccess { loadProducts() }
        }
    }

    fun updateProduct(productEntity: ProductEntity) {
        viewModelScope.launch {
            runCatching { repo.updateProduct(productEntity) }
                .onFailure { handleApiError(it, "Error al actualizar el producto.") }
                .onSuccess { loadProducts() }
        }
    }

    private fun handleApiError(e: Throwable, defaultMessage: String) {
        val errorMessage = when (e) {
            is HttpException -> "Error ${e.code()}: ${e.response()?.errorBody()?.string() ?: defaultMessage}"
            is IOException -> "Error de red: Imposible conectar con el servidor."
            else -> defaultMessage
        }
        _uiState.value = _uiState.value.copy(error = errorMessage, isLoading = false)
    }
}