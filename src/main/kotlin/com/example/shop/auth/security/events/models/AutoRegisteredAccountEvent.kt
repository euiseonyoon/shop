package com.example.shop.auth.security.events.models

import kotlinx.serialization.Serializable

@Serializable
data class AutoRegisteredAccountEvent(
    val email: String,
    val rawPassword: String,
)
