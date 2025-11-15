package com.example.appmovilfitquality.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Index para consultas rápidas por orden
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Int,
    val productId: Int,         // ID del producto comprado
    val productName: String,    // Nombre del producto al momento de la compra
    val productPrice: Double,   // Precio unitario al momento de la compra
    val quantity: Int
)
