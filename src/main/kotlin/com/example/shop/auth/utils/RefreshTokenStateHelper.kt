package com.example.shop.auth.utils

interface RefreshTokenStateHelper {
    fun validateRefreshToken(email: String, refreshTokenFromRequest: String)

    fun updateWithNewRefreshToken(email: String, newRefreshToken: String)
}
