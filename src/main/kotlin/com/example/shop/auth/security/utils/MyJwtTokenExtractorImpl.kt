package com.example.shop.auth.security.utils

import com.example.shop.auth.REFRESH_TOKEN_KEY
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class MyJwtTokenExtractorImpl : MyJwtTokenExtractor {
    override fun extractAccessTokenFromHeader(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization")
        return if (bearer != null && bearer.startsWith("Bearer ")) {
            bearer.substring(7)
        } else null
    }

    override fun extractRefreshTokenFromCookie(request: HttpServletRequest): String? {
        val cookies: Array<Cookie> = request.cookies ?: return null
        return cookies.firstOrNull { it.name == REFRESH_TOKEN_KEY }?.value
    }
}
