package com.example.shop.auth.security.utils

import com.example.shop.auth.REFRESH_TOKEN_KEY
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest

class MyJwtTokenExtractor {
    companion object {
        fun extractAccessTokenFromHeader(request: HttpServletRequest): String? {
            val bearer = request.getHeader("Authorization")
            return if (bearer != null && bearer.startsWith("Bearer ")) {
                bearer.substring(7)
            } else null
        }

        fun extractRefreshTokenFromCookie(request: HttpServletRequest): String? {
            val cookies: Array<Cookie> = request.cookies ?: return null
            return cookies.firstOrNull { it.name == REFRESH_TOKEN_KEY }?.value
        }
    }
}
