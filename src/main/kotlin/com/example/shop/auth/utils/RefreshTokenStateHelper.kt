package com.example.shop.auth.utils

interface RefreshTokenStateHelper {
    fun getStoredRefreshToken(accountId: Long): String?

    fun validateRefreshToken(accountId: Long, refreshTokenFromRequest: String)

    fun updateWithNewRefreshToken(accountId: Long, newRefreshToken: String)

    fun fallBackIfRedisUnavailable(accountId: Long, refreshToken: String, e: Throwable)
}
