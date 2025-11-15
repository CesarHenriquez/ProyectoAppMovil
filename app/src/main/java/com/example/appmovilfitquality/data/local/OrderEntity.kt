package com.example.appmovilfitquality.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,


    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val shippingAddress: String,



    val totalCLP: Double,
    val timestamp: Long = System.currentTimeMillis(), //Fecha de la orden


    val proofImageUri: String? = null,
    val delivered: Boolean = false
)