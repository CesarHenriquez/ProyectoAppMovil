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
import retrofit2.HttpException
import java.io.IOException

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


    private val _isSessionLoaded = MutableStateFlow(false)
    val isSessionLoaded: StateFlow<Boolean> = _isSessionLoaded.asStateFlow()

    private val _registerValidationState = MutableStateFlow(RegisterValidation())
    val registerValidationState: StateFlow<RegisterValidation> = _registerValidationState.asStateFlow()

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess.asStateFlow()


    init {
        viewModelScope.launch {
            session.roleFlow.collect { role ->
                if (role != null) {
                    _currentUserRole.value = role
                } else {
                    _currentUserRole.value = UserRole.GUEST
                }

                _isSessionLoaded.value = true
            }
        }
    }


    fun logout() {
        viewModelScope.launch { session.clearSession() }
        _currentUserRole.value = UserRole.GUEST
    }



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


            val nameError = Validators.validateName(nameTrimmed)
            val emailError = Validators.validateEmail(emailTrimmed)
            val phoneError = Validators.validatePhone(phoneTrimmed)
            val passwordError = Validators.validatePassword(password)


            val confirmError = when {
                confirm.isBlank() -> "Debe confirmar la contraseña"
                confirm != password -> "Las contraseñas no coinciden"
                else -> null
            }

            var currentValidation = RegisterValidation(
                nameError, emailError, phoneError, passwordError, confirmError
            )


            if (listOf(nameError, emailError, phoneError, passwordError, confirmError).any { it != null }) {
                _registerValidationState.value = currentValidation
                return@launch
            }


            try {

                repo.registerUser(
                    AuthRepository.User(
                        name = nameTrimmed,
                        email = emailTrimmed,
                        phone = phoneTrimmed,
                        password = password,

                        role = UserRole.CLIENTE
                    )
                )


                _registerValidationState.value = RegisterValidation()
                _registrationSuccess.value = true

            } catch (e: HttpException) {

                _registerValidationState.value = currentValidation.copy(emailError = "Error ${e.code()}: El email ya está registrado o hay un conflicto en el servidor.")
            } catch (e: IOException) {

                _registerValidationState.value = currentValidation.copy(generalError = "Error de red: Imposible conectar con el microservicio de registro.")
            } catch (e: Exception) {
                _registerValidationState.value = currentValidation.copy(generalError = "Error al registrar: ${e.message}")
            }
        }
    }

    fun consumeRegistrationSuccess() {
        _registrationSuccess.value = false
    }



    fun login(email: String, password: String, onResult: (LoginResult) -> Unit) {
        viewModelScope.launch {


            val cleanedEmail = email.trim()
            val cleanedPassword = password.trim()

            if (cleanedEmail.isBlank() || cleanedPassword.isBlank()) {
                onResult(LoginResult.WrongPassword)
                return@launch
            }
            try {

                val user = repo.login(cleanedEmail, cleanedPassword)


                _currentUserRole.value = user.role
                onResult(LoginResult.Success(user.role))

            } catch (e: HttpException) {

                onResult(LoginResult.WrongPassword)
            } catch (e: IOException) {

                onResult(LoginResult.WrongPassword)
            } catch (e: Exception) {
                onResult(LoginResult.WrongPassword)
            }
        }
    }
}