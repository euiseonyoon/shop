package com.example.shop.auth.security.events.models

data class AutoRegisteredAccountEvent(
    val email: String,
    val rawPassword: String,
)
