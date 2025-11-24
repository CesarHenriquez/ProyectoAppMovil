package com.example.appmovilfitquality.data.dto

import com.google.gson.annotations.SerializedName


data class UserDto(
    @SerializedName("usuario_id")
    val id: Int = 0,
    @SerializedName("nickname")
    val name: String,
    @SerializedName("correo")
    val email: String,

    val phone: String? = null,

    @SerializedName("rol")
    val rol: RolDto? = null,
)


data class RolDto(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("nombre")

    val nombre: String,
)


data class UserCredentialsDto(
    @SerializedName("correo")
    val email: String,
    @SerializedName("clave")
    val clave: String,
    @SerializedName("nickname")
    val nickname: String? = null
)


data class LoginResponseDto(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: UserDto,
)