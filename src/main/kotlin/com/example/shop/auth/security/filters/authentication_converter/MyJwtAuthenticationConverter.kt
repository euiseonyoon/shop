package com.example.shop.auth.security.filters.authentication_converter

import com.example.shop.auth.security.utils.MyJwtTokenExtractor
import jakarta.servlet.http.HttpServletRequest

interface MyJwtAuthenticationConverter {
    val myJwtTokenExtractor: MyJwtTokenExtractor

    fun extractAccessToken(request: HttpServletRequest): String? {
        return myJwtTokenExtractor.extractAccessTokenFromHeader(request)
    }
}
