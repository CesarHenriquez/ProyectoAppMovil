package com.example.appmovilfitquality.data.repository

import com.example.appmovilfitquality.data.dto.ProductDto
import com.example.appmovilfitquality.data.remote.ApiService
import com.example.appmovilfitquality.domain.model.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRepository(private val api: ApiService) {

    data class ProductEntity(
        val id: Int = 0,
        val name: String,
        val description: String,
        val price: Double,
        val stock: Int = 0,
        val imageResourceName: String? = null,
        val imageUri: String? = null
    )

    private fun ProductDto.toEntity(): ProductEntity {
        // ⬇️ CORRECCIÓN: Usamos el nombre de la imagen que viene del servidor.
        // .substringBeforeLast(".") asegura que si viene "cinturon_fit.jpg" o "cinturon_fit", funcione igual.
        val localResourceName = this.imageUri?.substringBeforeLast(".")

        return ProductEntity(
            id = this.id,
            name = this.name,
            description = this.description,
            price = this.price,
            // Si el stock viene nulo, ponemos 0
            stock = this.stock ?: 0,
            imageUri = this.imageUri,

            // ⬇️ AQUÍ ESTABA EL ERROR: Antes era null, ahora asignamos el nombre correcto ⬇️
            imageResourceName = localResourceName
        )
    }

    private fun ProductEntity.toDto(): ProductDto =
        ProductDto(
            id = this.id,
            name = this.name,
            description = this.description,
            price = this.price,
            stock = this.stock,
            imageUri = this.imageUri
        )

    fun getAllProducts(): Flow<List<ProductEntity>> = flow {
        val dtoList = api.getProducts()
        emit(dtoList.map { it.toEntity() })
    }

    suspend fun getProductById(id: Int): ProductEntity? {
        val products = api.getProducts()
        return products.find { it.id == id }?.toEntity()
    }

    suspend fun addProduct(product: ProductEntity) {
        api.addProduct(product = product.toDto())
    }

    suspend fun updateProduct(product: ProductEntity) {
        api.updateProduct(id = product.id, product = product.toDto())
    }

    suspend fun deleteProduct(product: ProductEntity) {
        api.deleteProduct(id = product.id)
    }

    suspend fun tryReduceStock(items: List<CartItem>): Boolean {
        return true
    }
}