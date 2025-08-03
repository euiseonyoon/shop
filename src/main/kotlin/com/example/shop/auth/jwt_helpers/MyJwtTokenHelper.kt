package com.example.shop.auth.jwt_helpers

import io.jsonwebtoken.Claims
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.GrantedAuthority

interface MyJwtTokenHelper {
    val accessTokenExpirationMs: Long
    val refreshTokenExpirationMs: Long
    val authClaimKey: String
    val authStringDelimiter: String

    fun createAccessToken(accountId: Long, authorities: List<GrantedAuthority>): String

    fun createRefreshToken(accountId: Long): String

    fun parseAccessToken(accessToken: String): Claims

    fun parseRefreshToken(refreshToken: String): Claims

    fun getAuthorityStringList(claims: Claims): List<String>

    fun getSubject(claims: Claims): Long

    fun setRefreshTokenOnCookie(response: HttpServletResponse, refreshToken: String)

    fun deleteRefreshTokenFromCookie(response: HttpServletResponse)
}
