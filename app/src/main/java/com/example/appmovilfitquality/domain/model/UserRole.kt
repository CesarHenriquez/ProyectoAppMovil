package com.example.appmovilfitquality.domain.model

enum class UserRole {
    CLIENTE,
    STOCK,
    DELIVERY,
    GUEST;

    companion object {

        private const val AUTHORIZED_STOCK_EMAIL = "admin@stock.com"
        private const val AUTHORIZED_DELIVERY_EMAIL = "cesar@delivery.com"



        fun getRoleFromEmail(email: String): UserRole {
            return when (email.lowercase()) {
                AUTHORIZED_STOCK_EMAIL -> STOCK
                AUTHORIZED_DELIVERY_EMAIL -> DELIVERY

                else -> CLIENTE
            }
        }
    }
}