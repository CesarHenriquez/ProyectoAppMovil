package com.example.appmovilfitquality.data.dto

import com.google.gson.annotations.SerializedName


data class OrderItemDto(
    @SerializedName("productoId")
    val productId: Int,
    @SerializedName("cantidad")
    val quantity: Int,
    @SerializedName("precioUnitario")
    val productPrice: Double,

    val productName: String? = null
)


data class OrderDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("usuarioId")
    val customerId: Long,
    @SerializedName("direccionId")
    val shippingAddressId: Long,
    @SerializedName("fecha")
    val fecha: String? = null,
    @SerializedName("detalles")
    val items: List<OrderItemDto>,

    @SerializedName("delivered")
    val delivered: Boolean = false,
    @SerializedName("proofUri")
    val proofUri: String? = null,
    val shippingAddressDetail: Long,
    val totalCLP: Double,
)


data class CreateOrderRequestDto(
    @SerializedName("usuarioId")
    val customerId: Int,
    @SerializedName("direccionId")
    val shippingAddressId: Int,
    @SerializedName("detalles")
    val items: List<OrderItemDto>
)
