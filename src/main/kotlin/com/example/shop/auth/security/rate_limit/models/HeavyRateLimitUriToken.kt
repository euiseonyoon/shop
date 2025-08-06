package com.example.shop.auth.security.rate_limit.models

data class HeavyRateLimitUriToken(
    var urlPattern: String = "",
    var tokens: Int = 1
)
