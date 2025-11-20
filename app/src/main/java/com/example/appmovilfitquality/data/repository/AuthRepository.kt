package com.example.appmovilfitquality.data.repository

import com.example.appmovilfitquality.data.local.UserDao
import com.example.appmovilfitquality.data.local.UserEntity
import com.example.appmovilfitquality.domain.model.UserRole


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
    //  Función auxiliar para convertir a Entity
    private fun User.toEntity(): UserEntity =
        UserEntity(
            name = this.name,
            email = this.email,
            password = this.password,
            phone = this.phone,
            role = this.role.name
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
    // Actualiza el perfil del usuario (nombre, teléfono, etc.)
    suspend fun update(user: User) {
        val existingEntity = userDao.getUserByEmail(user.email)
        if (existingEntity != null) {
            // Se actualizan los campos editables, manteniendo el ID y la contraseña original.
            val updatedEntity = existingEntity.copy(
                name = user.name,
                phone = user.phone,
                // Mantiene la contraseña original para que solo se cambie con updatePassword
                role = user.role.name
            )
            userDao.update(updatedEntity)
        }
    }

    // Actualiza solo la contraseña
    suspend fun updatePassword(email: String, newPassword: String) {
        val existingEntity = userDao.getUserByEmail(email)
        if (existingEntity != null) {
            val updatedEntity = existingEntity.copy(password = newPassword)
            userDao.update(updatedEntity)
        }
    }
}