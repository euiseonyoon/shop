package com.example.shop.security.models

import kotlinx.serialization.Serializable

@Serializable
data class ThirdPartyOauthTokenLoginRequest(
    val token: String,
)
