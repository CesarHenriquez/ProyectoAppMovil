package com.example.appmovilfitquality.domain.model

enum class UserRole {
    CLIENTE,
    STOCK,
    DELIVERY,
    GUEST; // Rol por defecto

    //  Asigna el rol basado en el dominio del email
    companion object {
        fun getRoleFromEmail(email: String): UserRole {
            return when {
                email.endsWith("@stock.com", ignoreCase = true) -> STOCK
                email.endsWith("@delivery.com", ignoreCase = true) -> DELIVERY
                // Todos los demás correos son CLIENTES por defecto
                else -> CLIENTE
            }
        }
    }
}