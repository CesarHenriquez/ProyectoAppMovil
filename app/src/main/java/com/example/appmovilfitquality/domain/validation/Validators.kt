package com.example.appmovilfitquality.domain.validation

private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
private val NAME_REGEX = "^[a-zA-Z\\s]+\$".toRegex()
private val PHONE_REGEX = "^[0-9]+\$".toRegex()
private val PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d).{8,}\$".toRegex()

object Validators {
    fun validateName(name: String): String? {
        if (name.isBlank()) return "El campo nombre no puede estar vacío"
        if (!name.matches(NAME_REGEX)) return "Solo letras y espacios"
        return null
    }
    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "El campo email no puede estar vacío"
        if (!email.matches(EMAIL_REGEX)) return "Formato de email inválido (ej: usuario@dominio.com)"
        return null
    }
    fun validatePhone(phone: String): String? {
        if (phone.isBlank()) return "El campo teléfono no puede estar vacío"
        if (!phone.matches(PHONE_REGEX) || phone.length !in 8..9) return "Solo números (8 o 9 dígitos)"
        return null
    }
    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "El campo contraseña no puede estar vacío"
        if (!password.matches(PASSWORD_REGEX)) return "Mín. 8 caracteres, 1 mayúscula y 1 número"
        return null
    }
}