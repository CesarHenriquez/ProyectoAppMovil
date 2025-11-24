package com.example.appmovilfitquality.data.repository

import com.example.appmovilfitquality.data.dto.CreateOrderRequestDto
import com.example.appmovilfitquality.data.dto.OrderDto
import com.example.appmovilfitquality.data.dto.OrderItemDto
import com.example.appmovilfitquality.data.remote.ApiService
import com.example.appmovilfitquality.domain.model.CartItem
import com.example.appmovilfitquality.domain.model.Order
import com.example.appmovilfitquality.domain.model.OrderItem
import kotlinx.coroutines.flow.first


class OrderRepository(
    private val api: ApiService,
    private val authRepo: AuthRepository
) {



    data class OrderEntity(
        val id: Int = 0,
        val customerName: String,
        val customerEmail: String,
        val customerPhone: String,
        val shippingAddress: String,
        val productSummary: String,
        val totalCLP: Double,
        val proofImageUri: String? = null,
        val delivered: Boolean = false
    )




    private fun OrderDto.toDomain(): Order {
        val orderItems = this.items.map { itemDto ->
            OrderItem(
                productId = itemDto.productId,
                productName = itemDto.productName ?: "Producto Desconocido",
                productPrice = itemDto.productPrice,
                quantity = itemDto.quantity
            )
        }


        val shippingAddressDetailString = "Dirección ID: ${this.shippingAddressDetail}"
        val customerNameString = "Usuario ID: ${this.customerId}"


        return Order(
            id = this.id,
            customerId = this.customerId.toInt(),
            customerName = customerNameString,
            customerEmail = "",
            customerPhone = "",
            shippingAddress = shippingAddressDetailString,
            totalCLP = this.totalCLP,
            timestamp = this.timestamp,
            delivered = this.delivered,
            proofImageUri = this.proofUri,
            items = orderItems
        )
    }




    suspend fun getOrdersByCustomerId(customerId: Long): List<Order> {

        val dtoList = api.getOrdersByCustomer(id = customerId)
        return dtoList.map { it.toDomain() }
    }


    suspend fun getAllOrders(): List<Order> {
        val dtoList = api.getAllOrders()
        return dtoList.map { it.toDomain() }
    }



    suspend fun placeOrder(userEmail: String, shippingAddress: String, items: List<CartItem>): Order {


        val realUserIdLong = authRepo.getCurrentUserId()
            ?: throw IllegalStateException("Fallo al obtener el ID del usuario para la orden.")

        val realUserId = realUserIdLong.toInt()

        val dummyAddressId = 1

        val orderItems = items.map { cartItem ->
            OrderItemDto(
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                productPrice = cartItem.product.price,
                quantity = cartItem.quantity
            )
        }

        val request = CreateOrderRequestDto(
            customerId = realUserId,
            shippingAddressId = dummyAddressId,
            items = orderItems
        )

        val orderDto = api.createOrder(request = request)
        return orderDto.toDomain()
    }


    suspend fun getOrdersForDelivery(): List<OrderEntity> {
        val allOrders = getAllOrders()

        return allOrders.map { order ->
            OrderEntity(
                id = order.id,
                customerName = order.customerName,
                customerEmail = order.customerEmail,
                customerPhone = order.customerPhone,
                shippingAddress = order.shippingAddress,
                productSummary = order.items.joinToString { "${it.productName} x${it.quantity}" },
                totalCLP = order.totalCLP,
                delivered = order.delivered
            )
        }
    }



    suspend fun saveDeliveryProof(orderId: Int, proofUri: String) {

        val requestBody = mapOf("proofUri" to proofUri)
        api.setDeliveryProof(orderId = orderId.toLong(), request = requestBody)
    }
}