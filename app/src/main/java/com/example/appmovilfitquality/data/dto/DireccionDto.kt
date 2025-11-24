package com.example.appmovilfitquality.data.dto


import com.google.gson.annotations.SerializedName


data class DireccionDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("calle")
    val calle: String,
    @SerializedName("numero")
    val numero: String,
    @SerializedName("codigoPostal")
    val codigoPostal: String

)