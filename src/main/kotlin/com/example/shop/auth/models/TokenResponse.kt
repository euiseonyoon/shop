package com.example.shop.auth.models

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val accessToken: String
)
