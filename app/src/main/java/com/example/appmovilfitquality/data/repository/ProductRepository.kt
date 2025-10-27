package com.example.appmovilfitquality.data.repository

import com.example.appmovilfitquality.data.local.ProductDao
import com.example.appmovilfitquality.data.local.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class ProductRepository(private val productDao: ProductDao) {

    // productos iniciales
    private val defaultProducts = listOf(
        ProductEntity(
            name = "Muñequeras FitQuality",
            description = "Soporte firme y cómodo para tus levantamientos.",
            price = 2500.0,
            imageResourceName = "munequeras_fit"
        ),
        ProductEntity(
            name = "Magnesio FitQuality",
            description = "Mejora el agarre en cada repetición.",
            price = 2500.0,
            imageResourceName = "magnesio_fit"
        ),
        ProductEntity(
            name = "Cinturón Powerlifter",
            description = "Cinturón de cuero reforzado para máxima estabilidad.",
            price = 10000.0,
            imageResourceName = "cinturon_fit"
        ),
        ProductEntity(
            name = "Straps FitQuality",
            description = "Correas resistentes para mejorar tu agarre.",
            price = 2500.0,
            imageResourceName = "straps_fit"
        )
    )


    fun getAllProducts(): Flow<List<ProductEntity>> = flow {
        val current = productDao.getAllProducts().first()
        if (current.isEmpty()) {
            defaultProducts.forEach { productDao.insertProduct(it) }
        }
        emitAll(productDao.getAllProducts())
    }

    suspend fun addProduct(product: ProductEntity) = productDao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)
}