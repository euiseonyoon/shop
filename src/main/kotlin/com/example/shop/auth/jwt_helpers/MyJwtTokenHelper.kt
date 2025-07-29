package com.example.shop.auth.jwt_helpers

interface MyJwtTokenHelper {
    fun createAccessToken(email: String): String

    fun createRefreshToken(email: String): String
}