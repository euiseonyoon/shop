package com.example.shop.security.jwt_helper

interface MyJwtTokenHelper {
    fun createAccessToken(email: String): String

    fun createRefreshToken(email: String): String
}