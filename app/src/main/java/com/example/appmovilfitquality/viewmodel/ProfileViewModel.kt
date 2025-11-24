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
import retrofit2.HttpException
import java.io.IOException

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

                sessionEmail = sessionManager.emailFlow.first()

                if (sessionEmail.isNullOrBlank()) {
                    _uiState.update { it.copy(isLoading = false, error = "Error de sesión: No se pudo obtener el email persistido.") }
                    return@launch
                }


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
                    _uiState.update { it.copy(isLoading = false, error = "Usuario no encontrado en la base de datos.") }
                }

            } catch (e: HttpException) {
                _uiState.update { it.copy(isLoading = false, error = "Error ${e.code()}: No se pudo cargar el perfil.") }
            } catch (e: IOException) {
                _uiState.update { it.copy(isLoading = false, error = "Error de red: Imposible conectar con el microservicio.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Fallo al cargar el perfil: ${e.message}") }
            }
        }
    }


    fun updateProfile(name: String, phone: String) {
        viewModelScope.launch {
            val email = _uiState.value.email
            if (email.isBlank()) return@launch


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
                    authRepo.updateProfile(updatedUser)
                    loadProfile()
                    _uiState.update { it.copy(message = "Perfil actualizado con éxito.") }
                }
            } catch (e: HttpException) {
                _uiState.update { it.copy(error = "Error ${e.code()}: No se pudo guardar el perfil.") }
            } catch (e: IOException) {
                _uiState.update { it.copy(error = "Error de red al guardar el perfil.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar los cambios.") }
            }
        }
    }

    // Flujo de cambio de contraseña (PUT /users/password)
    // ⬇️ CORRECCIÓN: Cambio de contraseña usando la nueva API ⬇️
    suspend fun changePassword(current: String, new: String, confirm: String) {
        _passwordValidationState.value = PasswordChangeValidation()
        val email = _uiState.value.email

        val newPasswordError = Validators.validatePassword(new)
        val confirmPasswordError = if (confirm != new) "Las nuevas contraseñas no coinciden" else null

        if (newPasswordError != null || confirmPasswordError != null) {
            _passwordValidationState.value = PasswordChangeValidation(
                newPasswordError = newPasswordError,
                confirmPasswordError = confirmPasswordError
            )
            return
        }

        try {
            // 1. **AUTENTICAR PRIMERO** (Simulamos una verificación de credenciales con la API de Login)
            // Esto asegura que 'current' password sea correcta.
            authRepo.login(email, current)

            // 2. Si la autenticación es exitosa, ACTUALIZAR la clave
            authRepo.updatePassword(email, new)
            _passwordValidationState.value = PasswordChangeValidation(success = true)
            _uiState.update { it.copy(message = "Contraseña cambiada con éxito.") }

        } catch (e: HttpException) {
            // ⬇️ CRÍTICO: El error 401 del login (clave actual incorrecta) se maneja aquí ⬇️
            if (e.code() == 401) {
                _passwordValidationState.value = PasswordChangeValidation(
                    currentPasswordError = "Contraseña actual incorrecta o email no encontrado."
                )
            } else {
                _passwordValidationState.value = PasswordChangeValidation(
                    generalError = "Error ${e.code()}: Fallo al actualizar la clave en el servidor."
                )
            }
        } catch (e: Exception) {
            _passwordValidationState.value = PasswordChangeValidation(
                generalError = "Error al cambiar la contraseña."
            )
        }
    }
}