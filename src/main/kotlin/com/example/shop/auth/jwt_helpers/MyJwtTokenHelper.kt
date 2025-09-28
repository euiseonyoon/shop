package com.example.shop.auth.jwt_helpers

import com.example.shop.auth.domain.Email
import io.jsonwebtoken.Claims
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.GrantedAuthority

interface MyJwtTokenHelper {
    companion object {
        const val EMAIL_CLAIM_KEY = "email"
        const val AUTH_CLAIM_KEY = "auth"
        const val AUTH_STRING_DELIMITER = ","
    }
    val accessTokenExpirationMs: Long
    val refreshTokenExpirationMs: Long

    fun createAccessToken(accountId: Long, authorities: Set<GrantedAuthority>, email: Email): String

    fun createRefreshToken(accountId: Long): String

    fun parseAccessToken(accessToken: String): Claims

    fun parseRefreshToken(refreshToken: String): Claims

    fun getEmail(claims: Claims): String

    fun getAuthorityStringList(claims: Claims): List<String>

    fun getSubject(claims: Claims): Long

    fun setRefreshTokenOnCookie(response: HttpServletResponse, refreshToken: String)

    fun deleteRefreshTokenFromCookie(response: HttpServletResponse)
}
