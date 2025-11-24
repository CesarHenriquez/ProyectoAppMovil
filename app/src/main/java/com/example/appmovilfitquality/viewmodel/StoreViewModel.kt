package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.repository.ProductRepository
import com.example.appmovilfitquality.data.repository.ProductRepository.ProductEntity
import com.example.appmovilfitquality.domain.model.Product
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

    // Función auxiliar para convertir de la entidad del Repo (datos) al modelo de Dominio (UI)
    private fun ProductEntity.toDomain() = Product(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        stock = this.stock,
        imageUri = this.imageUri,
        // Este nombre ya viene limpio desde el repositorio
        imageResourceName = this.imageResourceName ?: ""
    )

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repo.getAllProducts().collect { listEntity ->

                    // ⬇️⬇️⬇️ LOGS DE DEPURACIÓN ⬇️⬇️⬇️
                    android.util.Log.d("DEBUG_CATALOGO", "--------------------------------------------------")
                    android.util.Log.d("DEBUG_CATALOGO", "Respuesta del Repositorio recibida.")
                    android.util.Log.d("DEBUG_CATALOGO", "Cantidad de productos: ${listEntity.size}")

                    listEntity.forEach { p ->
                        android.util.Log.d("DEBUG_CATALOGO", "PRODUCTO: ID=${p.id} | Nombre='${p.name}' | ImgURI='${p.imageUri}' | ResName='${p.imageResourceName}'")
                    }
                    android.util.Log.d("DEBUG_CATALOGO", "--------------------------------------------------")
                    // ⬆️⬆️⬆️ FIN LOGS ⬆️⬆️⬆️

                    // Convertimos a dominio y actualizamos la UI
                    val listDomain = listEntity.map { it.toDomain() }
                    _uiState.value = StoreUiState(products = listDomain, isLoading = false)
                }
            } catch (e: HttpException) {
                val errorMsg = "Error HTTP ${e.code()}: ${e.message()}"
                android.util.Log.e("DEBUG_CATALOGO", errorMsg)
                _uiState.value = StoreUiState(error = errorMsg, isLoading = false)
            } catch (e: IOException) {
                val errorMsg = "Error de Red: Verifique conexión al microservicio."
                android.util.Log.e("DEBUG_CATALOGO", errorMsg)
                _uiState.value = StoreUiState(error = errorMsg, isLoading = false)
            } catch (e: Exception) {
                val errorMsg = "Error desconocido: ${e.message}"
                android.util.Log.e("DEBUG_CATALOGO", errorMsg)
                _uiState.value = StoreUiState(error = errorMsg, isLoading = false)
            }
        }
    }

    // --- Gestión de Stock (Admin) ---

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
        android.util.Log.e("DEBUG_CATALOGO", "API Error: $errorMessage")
        _uiState.value = _uiState.value.copy(error = errorMessage, isLoading = false)
    }
}