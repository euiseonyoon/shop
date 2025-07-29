package com.example.shop.auth.models

import kotlinx.serialization.Serializable

@Serializable
data class EmailPasswordLoginRequest(
    val email: String,
    val password: String,
)
