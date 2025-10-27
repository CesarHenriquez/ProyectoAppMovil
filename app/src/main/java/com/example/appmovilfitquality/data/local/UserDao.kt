package com.example.appmovilfitquality.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    // Función clave para el 'seed' (datos iniciales)
    @Query("SELECT COUNT(id) FROM users")
    suspend fun countUsers(): Int

    @Query("SELECT email FROM users WHERE role = :role LIMIT 1")
    suspend fun firstEmailByRole(role: String): String?
}