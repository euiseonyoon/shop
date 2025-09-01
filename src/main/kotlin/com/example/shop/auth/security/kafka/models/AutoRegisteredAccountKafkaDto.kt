package com.example.shop.auth.security.kafka.models

import kotlinx.serialization.Serializable

@Serializable
data class AutoRegisteredAccountKafkaDto(
    val email: String,
    val rawPassword: String,
)

