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

    // no restaura el rol desde DataStore al iniciar para evitar auto-login al abrir la app.

    fun logout() {
        viewModelScope.launch { session.clearSession() }
        _currentUserRole.value = UserRole.GUEST
    }

    // Registro con validaciones y asignación de rol (NO auto-login).
    fun validateAndRegister(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirm: String
    ) {
        viewModelScope.launch {
            _registrationSuccess.value = false

            val allEmpty = name.isBlank() && email.isBlank() && phone.isBlank() && password.isBlank() && confirm.isBlank()
            if (allEmpty) {
                _registerValidationState.value = RegisterValidation(generalError = "Favor complete formulario")
                return@launch
            }

            val nameError = Validators.validateName(name.trim())
            val emailError = Validators.validateEmail(email.trim())
            val phoneError = Validators.validatePhone(phone.trim())
            val passwordError = Validators.validatePassword(password)
            val confirmError = if (confirm.isBlank() || confirm != password) "Las contraseñas no coinciden" else null

            var currentValidation = RegisterValidation(
                nameError, emailError, phoneError, passwordError, confirmError
            )

            if (listOf(nameError, emailError, phoneError, passwordError, confirmError).any { it != null }) {
                _registerValidationState.value = currentValidation
                return@launch
            }

            try {
                if (repo.emailExists(email)) {
                    _registerValidationState.value = currentValidation.copy(emailError = "El email ya está registrado")
                    return@launch
                }

                val assignedRole = UserRole.getRoleFromEmail(email.trim())

                repo.saveUser(
                    AuthRepository.User(
                        name = name.trim(),
                        email = email.trim(),
                        phone = phone.trim(),
                        password = password,
                        role = assignedRole
                    )
                )

                // Éxito de registro: limpia errores y avisa al UI.
                _registerValidationState.value = RegisterValidation()

                //  NO guarda sesión ni cambia el rol aquí.
                // El usuario deberá ir a Login y autenticarse con sus credenciales.

                _registrationSuccess.value = true

            } catch (_: Exception) {
                currentValidation = currentValidation.copy(generalError = "Error de conexión: El sistema no está disponible.")
                _registerValidationState.value = currentValidation
            }
        }
    }

    //Consumir el evento de éxito para no re-triggerear navegación.
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
                    session.saveSession(email.trim(), user.role) // persiste sesión SOLO al loguear
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