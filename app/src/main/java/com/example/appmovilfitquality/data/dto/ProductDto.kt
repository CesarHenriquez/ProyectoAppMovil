package com.example.appmovilfitquality.data.dto

import com.google.gson.annotations.SerializedName


data class ProductDto(
    @SerializedName("producto_id")
    val id: Int = 0,
    @SerializedName("nombre")
    val name: String,
    @SerializedName("descripcion")
    val description: String,
    @SerializedName("precio")
    val price: Double,


    @SerializedName("stock")
    val stock: Int = 0,

    @SerializedName("imageUri")
    val imageUri: String? = null
)