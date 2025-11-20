package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val error: String? = null,
    val message: String? = null
)

data class PasswordChangeValidation(
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val success: Boolean = false
)

class ProfileViewModel(
    private val authRepo: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _passwordValidationState = MutableStateFlow(PasswordChangeValidation())
    val passwordValidationState: StateFlow<PasswordChangeValidation> = _passwordValidationState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            var sessionEmail: String? = null

            try {
                // 1. Intenta leer el email de la sesión DataStore
                sessionEmail = sessionManager.emailFlow.first()

                if (sessionEmail.isNullOrBlank()) {
                    _uiState.update { it.copy(isLoading = false, error = "Error de sesión: No se pudo obtener el email persistido.") }
                    return@launch
                }

                // 2. Busca el usuario en Room con el email obtenido
                val user = authRepo.getUserByEmail(sessionEmail)

                if (user != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = user.name,
                            email = user.email,
                            phone = user.phone,
                            role = user.role.name,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Usuario ${sessionEmail} no encontrado en la base de datos.") }
                }

            } catch (e: Exception) {
                // Captura cualquier fallo de IO o lectura asíncrona.
                _uiState.update { it.copy(isLoading = false, error = "Fallo crítico al cargar la sesión: ${e.message}") }
            }
        }
    }

    // Actualiza los datos de perfil (nombre y teléfono)
    fun updateProfile(name: String, phone: String) {
        viewModelScope.launch {
            val email = _uiState.value.email
            if (email.isBlank()) return@launch

            // Validaciones rápidas
            val nameError = Validators.validateName(name.trim())
            val phoneError = Validators.validatePhone(phone.trim())

            if (nameError != null || phoneError != null) {
                _uiState.update { it.copy(error = nameError ?: phoneError) }
                return@launch
            }

            try {
                val existingUser = authRepo.getUserByEmail(email)
                if (existingUser != null) {
                    val updatedUser = existingUser.copy(
                        name = name.trim(),
                        phone = phone.trim()
                    )
                    authRepo.update(updatedUser)
                    loadProfile() // Recargar para actualizar la UI
                    _uiState.update { it.copy(message = "Perfil actualizado con éxito.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar los cambios.") }
            }
        }
    }

    // Flujo de cambio de contraseña
    suspend fun changePassword(current: String, new: String, confirm: String) {
        _passwordValidationState.value = PasswordChangeValidation()
        val email = _uiState.value.email

        val newPasswordError = Validators.validatePassword(new)
        val confirmPasswordError = if (confirm != new) "Las nuevas contraseñas no coinciden" else null

        // 1. Validar formato de la nueva contraseña
        if (newPasswordError != null || confirmPasswordError != null) {
            _passwordValidationState.value = PasswordChangeValidation(
                newPasswordError = newPasswordError,
                confirmPasswordError = confirmPasswordError
            )
            return
        }

        try {
            val user = authRepo.getUserByEmail(email)

            // 2. Verificar contraseña actual
            if (user?.password != current) {
                _passwordValidationState.value = PasswordChangeValidation(
                    currentPasswordError = "Contraseña actual incorrecta."
                )
                return
            }

            // 3. Actualizar contraseña
            authRepo.updatePassword(email, new)
            _passwordValidationState.value = PasswordChangeValidation(success = true)

        } catch (e: Exception) {
            _passwordValidationState.value = PasswordChangeValidation(
                generalError = "Error al cambiar la contraseña."
            )
        }
    }
}