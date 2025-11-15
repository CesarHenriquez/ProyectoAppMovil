package com.example.appmovilfitquality.data.local

import androidx.room.*

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders ORDER BY id DESC")
    suspend fun getAll(): List<OrderEntity>
    //  Órdenes por cliente (Historial de Compras)
    @Query("SELECT * FROM orders WHERE customerEmail = :email ORDER BY timestamp DESC")
    suspend fun getByCustomerEmail(email: String): List<OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<OrderEntity>)

    @Update
    suspend fun update(order: OrderEntity)

    @Query("UPDATE orders SET proofImageUri = :proofUri, delivered = 1 WHERE id = :orderId")
    suspend fun setDeliveryProof(orderId: Int, proofUri: String)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteById(orderId: Int)
}