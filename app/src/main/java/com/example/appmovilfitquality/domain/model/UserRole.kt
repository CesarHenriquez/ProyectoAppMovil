package com.example.appmovilfitquality.domain.model

enum class UserRole {
    CLIENTE,
    STOCK,
    DELIVERY,
    GUEST; // Rol por defecto

    companion object {
        // DEFINIR EMAILS AUTORIZADOS INTERNOS y EXCLUSIVOS
        private const val AUTHORIZED_STOCK_EMAIL = "admin@stock.com"
        private const val AUTHORIZED_DELIVERY_EMAIL = "cesar@delivery.com"

        // Asigna el rol basado en emails preautorizados.
        // Todos los demás emails se registran como cliente por defecto.

        fun getRoleFromEmail(email: String): UserRole {
            return when (email.lowercase()) {
                AUTHORIZED_STOCK_EMAIL -> STOCK
                AUTHORIZED_DELIVERY_EMAIL -> DELIVERY
                // Todos los demás correos son cliente
                else -> CLIENTE
            }
        }
    }
}