package com.example.appmovilfitquality.data.repository


import com.example.appmovilfitquality.data.dto.RolDto
import com.example.appmovilfitquality.data.dto.UserCredentialsDto
import com.example.appmovilfitquality.data.dto.UserDto
import com.example.appmovilfitquality.data.remote.ApiService
import com.example.appmovilfitquality.domain.model.UserRole
import com.example.appmovilfitquality.data.localstore.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filter

class AuthRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {

    data class User(
        val name: String,
        val email: String,
        val phone: String,
        val password: String,
        val role: UserRole,
        val token: String? = null
    )

    // ⬇️ NUEVA FUNCIÓN AUXILIAR: Mapeo seguro de String a Enum ⬇️
    private fun mapToDomainRole(roleName: String?): UserRole {
        return when (roleName?.uppercase()) {
            "ADMINISTRADOR", "ADMIN" -> UserRole.STOCK // "ADMINISTRADOR" del backend es "STOCK" en la App
            "DELIVERY" -> UserRole.DELIVERY
            "CLIENTE" -> UserRole.CLIENTE
            else -> UserRole.CLIENTE // Por defecto, si no coincide, es cliente
        }
    }

    // --- Mapeo DTO <-> Dominio ---

    private fun UserDto.toDomainModel(password: String = "", token: String? = null): User {
        // Usamos la función auxiliar segura
        val roleNameString = this.rol?.nombre
        val domainRole = mapToDomainRole(roleNameString)

        return User(
            name = this.name ?: "",
            email = this.email ?: "",
            phone = this.phone ?: "",
            password = password,
            role = domainRole,
            token = token
        )
    }

    // --- Funcionalidades ---

    suspend fun registerUser(user: User): User {
        val credentials = UserCredentialsDto(
            email = user.email,
            clave = user.password,
            nickname = user.name
        )
        val responseDto = api.register(user = credentials)
        return responseDto.toDomainModel(password = user.password)
    }

    suspend fun login(email: String, password: String): User {
        val credentials = UserCredentialsDto(
            email = email.trim(),
            clave = password.trim(),
            nickname = null
        )

        // 1. Llamada a la API
        val response = api.login(credentials = credentials)

        // 2. Mapeo Seguro del Rol (Aquí es donde fallaba el Admin)
        val roleNameString = response.user.rol?.nombre
        val userRole = mapToDomainRole(roleNameString)

        // 3. Guardar Sesión
        sessionManager.saveSession(response.user.id, response.user.email, userRole, response.token)

        return response.user.toDomainModel(password = password, token = response.token)
    }

    suspend fun getCurrentUserId(): Long? {
        return sessionManager.userIdFlow
            .filter { it != null && it > 0L }
            .first()
    }

    suspend fun getUserByEmail(email: String): User {
        val userDto = api.getUserByEmail(email = email)
        val currentToken = sessionManager.tokenFlow.first()
        return userDto.toDomainModel(password = "", token = currentToken)
    }

    suspend fun updateProfile(user: User) {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("ID de usuario no encontrado en la sesión.")

        val userDto = UserDto(
            id = userId.toInt(),
            name = user.name,
            email = user.email,
            phone = user.phone,
            rol = RolDto(nombre = user.role.name)
        )
        api.updateProfile(id = userId, userDto = userDto)
        sessionManager.updateSessionRole(user.role)
    }

    suspend fun updatePassword(email: String, newPassword: String) {
        val requestBody = mapOf("nuevaClave" to newPassword.trim())
        api.updatePassword(email = email.trim(), request = requestBody)
    }

    suspend fun clearSession() {
        sessionManager.clearSession()
    }
}
