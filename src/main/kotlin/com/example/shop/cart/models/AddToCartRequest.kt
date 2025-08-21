package com.example.shop.cart.models

import jakarta.validation.constraints.Positive
import kotlinx.serialization.Serializable

@Serializable
data class AddToCartRequest(
    val productId: Long,
    @field:Positive
    val quantity: Int,
)
