package com.example.appmovilfitquality.data.repository

import com.example.appmovilfitquality.data.local.OrderDao
import com.example.appmovilfitquality.data.local.OrderEntity

class DeliveryRepository(
    private val orderDao: OrderDao
) {
    suspend fun getOrders(): List<OrderEntity> = orderDao.getAll()

    // Crear pedido desde Checkout: retorna el ID generado por Room
    suspend fun placeOrder(order: OrderEntity): Long = orderDao.insert(order)

    // Guardar comprobante (foto) y marcar como entregado
    suspend fun saveDeliveryProof(orderId: Int, proofUri: String) {
        orderDao.setDeliveryProof(orderId, proofUri)
    }

    // Utilidades opcionales (por si las necesitas)
    suspend fun update(order: OrderEntity) = orderDao.update(order)
    suspend fun delete(orderId: Int) = orderDao.deleteById(orderId)
}