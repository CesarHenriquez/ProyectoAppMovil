package com.example.appmovilfitquality.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.domain.model.UserRole
import com.example.appmovilfitquality.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterValidation(
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val generalError: String? = null
)

sealed class LoginResult {
    data class Success(val role: UserRole) : LoginResult()
    object UserNotFound : LoginResult()
    object WrongPassword : LoginResult()
}


class AuthViewModel(
    private val repo: AuthRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _currentUserRole = MutableStateFlow<UserRole>(UserRole.GUEST)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _registerValidationState = MutableStateFlow(RegisterValidation())
    val registerValidationState: StateFlow<RegisterValidation> = _registerValidationState.asStateFlow()

    // Señal de éxito real de registro (para navegar a Login)
    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess.asStateFlow()

    fun logout() {
        viewModelScope.launch { session.clearSession() }
        _currentUserRole.value = UserRole.GUEST
    }


    //Valida todos los campos, verifica la existencia del email y asigna el rol seguro.

    fun validateAndRegister(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirm: String
    ) {
        viewModelScope.launch {
            _registrationSuccess.value = false

            val nameTrimmed = name.trim()
            val emailTrimmed = email.trim()
            val phoneTrimmed = phone.trim()

            // 1. Validaciones por Campo (Usando Validators.kt)
            val nameError = Validators.validateName(nameTrimmed)
            val emailError = Validators.validateEmail(emailTrimmed)
            val phoneError = Validators.validatePhone(phoneTrimmed)
            val passwordError = Validators.validatePassword(password)

            // 2. Validación de Coincidencia de Contraseñas
            val confirmError = when {
                confirm.isBlank() -> "Debe confirmar la contraseña"
                confirm != password -> "Las contraseñas no coinciden"
                else -> null
            }

            var currentValidation = RegisterValidation(
                nameError, emailError, phoneError, passwordError, confirmError
            )

            // Si hay cualquier error de campo, publica el estado y detiene el registro
            if (listOf(nameError, emailError, phoneError, passwordError, confirmError).any { it != null }) {
                _registerValidationState.value = currentValidation
                return@launch
            }

            // 3. Verificación de Exclusividad (BD)
            try {
                if (repo.emailExists(emailTrimmed)) {
                    _registerValidationState.value = currentValidation.copy(emailError = "El email ya está registrado")
                    return@launch
                }

                // 4. Asignación de Rol Seguro (Utiliza la lógica de UserRole.kt)
                val assignedRole = UserRole.getRoleFromEmail(emailTrimmed)

                repo.saveUser(
                    AuthRepository.User(
                        name = nameTrimmed,
                        email = emailTrimmed,
                        phone = phoneTrimmed,
                        password = password,
                        role = assignedRole
                    )
                )

                // Éxito: limpia errores y avisa al UI.
                _registerValidationState.value = RegisterValidation()
                _registrationSuccess.value = true

            } catch (e: Exception) {
                // Error de conexión/base de datos
                currentValidation = currentValidation.copy(generalError = "Error al conectar: El sistema no está disponible. Detalles: ${e.message}")
                _registerValidationState.value = currentValidation
            }
        }
    }

    fun consumeRegistrationSuccess() {
        _registrationSuccess.value = false
    }


    fun login(email: String, password: String, onResult: (LoginResult) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                onResult(LoginResult.WrongPassword)
                return@launch
            }
            try {
                val user = repo.getUserByEmail(email)
                if (user == null) {
                    onResult(LoginResult.UserNotFound)
                } else if (user.password == password) {
                    session.saveSession(email.trim(), user.role)
                    _currentUserRole.value = user.role
                    onResult(LoginResult.Success(user.role))
                } else {
                    onResult(LoginResult.WrongPassword)
                }
            } catch (_: Exception) {
                onResult(LoginResult.WrongPassword)
            }
        }
    }
}