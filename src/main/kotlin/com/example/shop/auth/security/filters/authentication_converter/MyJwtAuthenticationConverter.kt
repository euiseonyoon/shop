package com.example.shop.auth.security.filters.authentication_converter

import jakarta.servlet.http.HttpServletRequest

interface MyJwtAuthenticationConverter {
    fun extractAccessToken(request: HttpServletRequest): String?
}
