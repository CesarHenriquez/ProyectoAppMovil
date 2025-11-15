package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmovilfitquality.data.local.ProductEntity
import com.example.appmovilfitquality.domain.model.CartItem
import com.example.appmovilfitquality.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Clase de resultado para comunicar éxito/error a la vista
data class StockResult(
    val success: Boolean,
    val message: String? = null
)


// CartViewModel se mantiene simple, sin inyecciones extra.
class CartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> = _cartTotal.asStateFlow()

    // Mapear ProductEntity (Room) -> Product (dominio)
    private fun ProductEntity.toDomain(): Product =
        Product(
            id = this.id,
            name = this.name,
            description = this.description,
            price = this.price,
            imageResourceName = this.imageResourceName ?: ""
        )

    // Intenta añadir 1 unidad al carrito, validando el stock disponible. Retorna StockResult para que la vista muestre un mensaje si es necesario


    suspend fun tryAddToCart(productEntity: ProductEntity): StockResult {
        val product = productEntity.toDomain()

        val currentStock = productEntity.stock
        val currentQuantityInCart = _cartItems.value.find { it.product.id == product.id }?.quantity ?: 0
        val requestedQuantity = 1

        if (currentStock <= 0) {
            return StockResult(success = false, message = "${product.name} está agotado.")
        }

        // VALIDACIÓN DE STOCK
        // Bloquear si la nueva cantidad excede el stock total
        if (currentQuantityInCart + requestedQuantity > currentStock) {
            val available = currentStock - currentQuantityInCart
            return StockResult(
                success = false,
                message = "Stock insuficiente. Solo quedan $available unidades en stock de ${product.name}."
            )
        }

        // Si hay stock disponible, procede a añadir
        _cartItems.update { current ->
            val existing = current.find { it.product.id == product.id }
            if (existing != null) {
                current.map { item ->
                    if (item.product.id == product.id) item.copy(quantity = item.quantity + requestedQuantity)
                    else item
                }
            } else {
                current + CartItem(product = product, quantity = requestedQuantity)
            }
        }
        updateCartTotal()
        return StockResult(success = true)
    }


    fun removeFromCart(productId: Int) {
        _cartItems.update { current ->
            val existing = current.find { it.product.id == productId }
            if (existing != null) {
                if (existing.quantity > 1) {
                    current.map { item ->
                        if (item.product.id == productId) item.copy(quantity = item.quantity - 1)
                        else item
                    }
                } else {
                    current.filter { it.product.id != productId }
                }
            } else {
                current.filter { it.product.id != productId }
            }
        }
        updateCartTotal()
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        updateCartTotal()
    }

    private fun updateCartTotal() {
        _cartTotal.value = _cartItems.value.sumOf { it.subtotal }
    }
}