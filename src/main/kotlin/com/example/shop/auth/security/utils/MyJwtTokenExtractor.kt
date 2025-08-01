package com.example.shop.auth.security.utils

import jakarta.servlet.http.HttpServletRequest

interface MyJwtTokenExtractor {
    fun extractAccessTokenFromHeader(request: HttpServletRequest): String?

    fun extractRefreshTokenFromCookie(request: HttpServletRequest): String?
}
