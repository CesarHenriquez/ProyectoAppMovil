package com.example.appmovilfitquality.domain.model

data class OrderItem(
    val productId: Int,
    val productName: String,
    val productPrice: Double,
    val quantity: Int
) {
    val subtotal: Double
        get() = productPrice * quantity
}
data class Order(
    val id: Int,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val shippingAddress: String,
    val totalCLP: Double,
    val timestamp: Long,
    val proofImageUri: String? = null,
    val delivered: Boolean = false,
    val items: List<OrderItem> //  Lista de items de la orden
)
