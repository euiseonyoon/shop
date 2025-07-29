package com.example.shop.auth.models

import kotlinx.serialization.Serializable

@Serializable
data class ThirdPartyOauthTokenLoginRequest(
    val token: String,
)
