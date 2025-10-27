package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmovilfitquality.data.local.ProductEntity
import com.example.appmovilfitquality.domain.model.CartItem
import com.example.appmovilfitquality.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> = _cartTotal.asStateFlow()

    // Mapear ProductEntity (Room) -> Product (dominio) para mantener la UI existente
    private fun ProductEntity.toDomain(): Product =
        Product(
            id = this.id,
            name = this.name,
            description = this.description,
            price = this.price,
            imageResourceName = this.imageResourceName ?: ""
        )

    // recibe ProductEntity desde StoreScreen
    fun addToCart(productEntity: ProductEntity) {
        val product = productEntity.toDomain()

        _cartItems.update { current ->
            val existing = current.find { it.product.id == product.id }
            if (existing != null) {
                current.map { item ->
                    if (item.product.id == product.id) item.copy(quantity = item.quantity + 1)
                    else item
                }
            } else {
                current + CartItem(product = product, quantity = 1)
            }
        }
        updateCartTotal()
    }

    fun removeFromCart(productId: Int) {
        _cartItems.update { current -> current.filter { it.product.id != productId } }
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