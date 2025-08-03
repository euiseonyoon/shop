package com.example.shop.auth.utils

interface RefreshTokenStateHelper {
    fun validateRefreshToken(accountId: Long, refreshTokenFromRequest: String)

    fun updateWithNewRefreshToken(accountId: Long, newRefreshToken: String)
}
