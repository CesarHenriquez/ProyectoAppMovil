package com.example.appmovilfitquality.data.repository

import com.example.appmovilfitquality.data.local.ProductDao
import com.example.appmovilfitquality.data.local.ProductEntity
import com.example.appmovilfitquality.domain.model.CartItem // Necesario para tryReduceStock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class ProductRepository(private val productDao: ProductDao) {

    // productos iniciales (MANTENIDOS y con campo 'stock' añadido)
    private val defaultProducts = listOf(
        ProductEntity(
            name = "Muñequeras FitQuality",
            description = "Soporte firme y cómodo para tus levantamientos.",
            price = 2500.0,
            stock = 50,
            imageResourceName = "munequeras_fit"
        ),
        ProductEntity(
            name = "Magnesio FitQuality",
            description = "Mejora el agarre en cada repetición.",
            price = 2500.0,
            stock = 30,
            imageResourceName = "magnesio_fit"
        ),
        ProductEntity(
            name = "Cinturón Powerlifter",
            description = "Cinturón de cuero reforzado para máxima estabilidad.",
            price = 10000.0,
            stock = 40,
            imageResourceName = "cinturon_fit"
        ),
        ProductEntity(
            name = "Straps FitQuality",
            description = "Correas resistentes para mejorar tu agarre.",
            price = 2500.0,
            stock = 75,
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

    // Obtener producto por ID
    suspend fun getProductById(id: Int): ProductEntity? = productDao.getProductById(id)

    suspend fun addProduct(product: ProductEntity) = productDao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

   //Valida stock
    suspend fun tryReduceStock(items: List<CartItem>): Boolean {
        //  Fase de Validación: Revisa si hay stock suficiente para todos los ítems
        for (item in items) {
            val entity = productDao.getProductById(item.product.id)
            if (entity == null || entity.stock < item.quantity) {
                // Falló la validación: stock insuficiente
                return false
            }
        }

        // 2. Fase de Ejecución: si es suficiente el stock, reduce
        for (item in items) {

            val entity = productDao.getProductById(item.product.id)!!
            val newStock = entity.stock - item.quantity
            val updatedEntity = entity.copy(stock = newStock)
            productDao.updateProduct(updatedEntity)
        }
        return true
    }
}