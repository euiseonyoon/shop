package com.example.shop.auth.jwt_helpers

import io.jsonwebtoken.Claims
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.GrantedAuthority

interface MyJwtTokenHelper {
    val accessTokenExpirationMs: Long
    val refreshTokenExpirationMs: Long
    val authClaimKey: String
    val authStringDelimiter: String

    fun createAccessToken(email: String, authorities: List<GrantedAuthority>): String

    fun createRefreshToken(email: String): String

    fun parseAccessToken(accessToken: String): Claims

    fun parseRefreshToken(refreshToken: String): Claims

    fun getAuthorityStringList(claims: Claims): List<String>

    fun getAccountEmail(claims: Claims): String

    fun setRefreshTokenOnCookie(response: HttpServletResponse, refreshToken: String)

    fun deleteRefreshTokenFromCookie(response: HttpServletResponse)
}
