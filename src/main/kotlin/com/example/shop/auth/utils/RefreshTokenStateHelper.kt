package com.example.shop.auth.utils

import com.example.shop.auth.security.domain.HashedRefreshToken

interface RefreshTokenStateHelper {
    fun getStoredRefreshToken(accountId: Long): HashedRefreshToken?

    fun validateRefreshToken(accountId: Long, refreshTokenFromRequest: String)

    fun updateWithNewRefreshToken(accountId: Long, hashedRefreshToken: HashedRefreshToken)

    fun fallBackIfRedisUnavailable(accountId: Long, refreshToken: String, e: Throwable)
}
