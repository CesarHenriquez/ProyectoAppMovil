package com.example.appmovilfitquality.data.repository

import com.example.appmovilfitquality.data.local.UserDao
import com.example.appmovilfitquality.data.local.UserEntity
import com.example.appmovilfitquality.domain.model.UserRole

/**
 * Versión migrada a Room.
 */
class AuthRepository(private val userDao: UserDao) {


    data class User(
        val name: String,
        val email: String,
        val phone: String,
        val password: String,
        val role: UserRole
    )


    private fun UserEntity.toDomainModel(): User =
        User(
            name = this.name,
            email = this.email,
            phone = this.phone,
            password = this.password,
            role = UserRole.valueOf(this.role)
        )

    // Intenta registrar. Retorna true si el email ya existe.
    suspend fun emailExists(email: String): Boolean {
        return userDao.getUserByEmail(email) != null
    }

    // Guarda un nuevo usuario.
    suspend fun saveUser(user: User) {
        val entity = UserEntity(
            name = user.name,
            email = user.email,
            password = user.password,
            phone = user.phone,
            role = user.role.name // Guarda el enum como String
        )
        userDao.insertUser(entity)
    }

    // Obtiene el usuario para login y retorna el modelo de Dominio.
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toDomainModel()
    }
}