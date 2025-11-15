package com.example.appmovilfitquality.data.repository

import com.example.appmovilfitquality.data.local.OrderDao
import com.example.appmovilfitquality.data.local.OrderEntity
import com.example.appmovilfitquality.data.local.OrderItemDao
import com.example.appmovilfitquality.data.local.OrderItemEntity
import com.example.appmovilfitquality.domain.model.CartItem
import com.example.appmovilfitquality.domain.model.Order
import com.example.appmovilfitquality.domain.model.OrderItem

class OrderRepository(
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao
) {
    // --- Conversiones ---

    private fun OrderEntity.toDomain(items: List<OrderItemEntity>): Order =
        Order(
            id = this.id,
            customerName = this.customerName,
            customerEmail = this.customerEmail,
            customerPhone = this.customerPhone,
            shippingAddress = this.shippingAddress,
            totalCLP = this.totalCLP,
            timestamp = this.timestamp,
            proofImageUri = this.proofImageUri,
            delivered = this.delivered,
            items = items.map { it.toDomain() }
        )

    private fun OrderItemEntity.toDomain(): OrderItem =
        OrderItem(
            productId = this.productId,
            productName = this.productName,
            productPrice = this.productPrice,
            quantity = this.quantity
        )


    // --- Operaciones de Historial ---

    // Historial de Compras (Cliente)
    suspend fun getOrdersByCustomer(email: String): List<Order> {
        return orderDao.getByCustomerEmail(email).map { entity ->
            val items = orderItemDao.getItemsByOrderId(entity.id)
            entity.toDomain(items)
        }
    }

    // Historial de Ventas (Admin)
    suspend fun getAllOrders(): List<Order> {
        return orderDao.getAll().map { entity ->
            val items = orderItemDao.getItemsByOrderId(entity.id)
            entity.toDomain(items)
        }
    }


    // --- Operaciones de Pedido/Entrega ---

    // recibe ítems del carrito y guarda OrderEntity + OrderItemEntity
    suspend fun placeOrder(orderEntity: OrderEntity, items: List<CartItem>): Long {
        val orderId = orderDao.insert(orderEntity).toInt()
        val orderItems = items.map { cartItem ->
            OrderItemEntity(
                orderId = orderId,
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                productPrice = cartItem.product.price,
                quantity = cartItem.quantity
            )
        }
        orderItemDao.insertAll(orderItems)
        return orderId.toLong()
    }

    suspend fun saveDeliveryProof(orderId: Int, proofUri: String) {
        orderDao.setDeliveryProof(orderId, proofUri)
    }


    suspend fun getOrdersForDelivery(): List<OrderEntity> = orderDao.getAll()


    suspend fun update(order: OrderEntity) = orderDao.update(order)
    suspend fun delete(orderId: Int) = orderDao.deleteById(orderId)
}