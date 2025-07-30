package com.example.shop.auth.jwt_helpers

import org.springframework.security.core.Authentication

interface MyJwtTokenHelper {
    val accessTokenExpirationMs: Long
    val refreshTokenExpirationMs: Long
    val authClaimKey: String

    fun createAccessToken(email: String, authentication: Authentication): String

    fun createRefreshToken(email: String): String
}