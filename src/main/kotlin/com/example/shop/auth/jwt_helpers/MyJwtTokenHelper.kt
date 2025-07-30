package com.example.shop.auth.jwt_helpers

import org.springframework.security.core.Authentication

interface MyJwtTokenHelper {
    fun createAccessToken(email: String, authentication: Authentication): String

    fun createRefreshToken(email: String): String
}