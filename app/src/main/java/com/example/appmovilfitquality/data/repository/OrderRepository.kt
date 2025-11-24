package com.example.appmovilfitquality.data.repository

import com.example.appmovilfitquality.data.dto.CreateOrderRequestDto
import com.example.appmovilfitquality.data.dto.OrderDto
import com.example.appmovilfitquality.data.dto.OrderItemDto
import com.example.appmovilfitquality.data.remote.ApiService
import com.example.appmovilfitquality.data.remote.MicroserviceUrls // ⬅️ Importante para construir la URL
import com.example.appmovilfitquality.domain.model.CartItem
import com.example.appmovilfitquality.domain.model.Order
import com.example.appmovilfitquality.domain.model.OrderItem
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Implementación REST para gestión de órdenes y pedidos (Ventas).
 */
class OrderRepository(
    private val api: ApiService,
    private val authRepo: AuthRepository
) {

    // --- Definición de Modelo de Datos Intermedio para DeliveryScreen ---
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

    // --- Mapeo DTO -> Dominio ---
    private fun OrderDto.toDomain(): Order {
        val orderItems = this.items.map { itemDto ->
            OrderItem(
                productId = itemDto.productId,
                productName = itemDto.productName ?: "Producto Desconocido",
                productPrice = itemDto.productPrice,
                quantity = itemDto.quantity
            )
        }

        // Mapeo de campos auxiliares (IDs) a String
        val shippingAddressDetailString = "Dirección ID: ${this.shippingAddressDetail}"
        val customerNameString = "Usuario ID: ${this.customerId}"

        // Lógica de conversión de Fecha (String "yyyy-MM-dd" -> Long timestamp)
        var timestampLong = System.currentTimeMillis()
        try {
            if (!this.fecha.isNullOrBlank()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(this.fecha)
                if (date != null) {
                    timestampLong = date.time
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Order(
            id = this.id,
            customerId = this.customerId.toInt(),
            customerName = customerNameString,
            customerEmail = "",
            customerPhone = "",
            shippingAddress = shippingAddressDetailString,
            totalCLP = this.totalCLP,
            timestamp = timestampLong,
            delivered = this.delivered,
            proofImageUri = this.proofUri,
            items = orderItems
        )
    }

    // --- Operaciones de Historial ---

    /**
     * Historial de Compras (Cliente) - Obtiene órdenes por ID de Usuario.
     */
    suspend fun getOrdersByCustomerId(customerId: Long): List<Order> {
        // ⬇️ CORRECCIÓN: Construimos la URL completa manualmente ⬇️
        // Esto evita el conflicto entre @Url y @Path en Retrofit.
        val fullUrl = "${MicroserviceUrls.VENTAS}api/ventas/usuario/$customerId"

        // Llamamos a la API pasando la URL ya construida
        val dtoList = api.getOrdersByCustomer(url = fullUrl)

        return dtoList.map { it.toDomain() }
    }

    /**
     * Historial de Ventas (Admin) - GET /ventas
     */
    suspend fun getAllOrders(): List<Order> {
        val dtoList = api.getAllOrders()
        return dtoList.map { it.toDomain() }
    }

    // --- Operaciones de Pedido/Checkout ---

    /**
     * Crea el pedido en el servidor (Checkout) - POST /ventas
     */
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

    /**
     * Obtiene órdenes para DeliveryScreen (GET /ventas).
     */
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

    /**
     * Marca la orden como entregada en el servidor.
     */
    suspend fun saveDeliveryProof(orderId: Int, proofUri: String) {
        val requestBody = mapOf("proofUri" to proofUri)
        api.setDeliveryProof(orderId = orderId.toLong(), request = requestBody)
    }
}