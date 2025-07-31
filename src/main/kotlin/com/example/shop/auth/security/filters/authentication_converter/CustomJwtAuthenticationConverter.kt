package com.example.shop.auth.security.filters.authentication_converter

import com.example.shop.auth.security.utils.MyJwtTokenExtractor
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.security.web.authentication.AuthenticationConverter

class CustomJwtAuthenticationConverter : AuthenticationConverter, MyJwtAuthenticationConverter {
    override fun convert(request: HttpServletRequest): Authentication? {
        val accessToken = extractAccessToken(request) ?: return null
        return BearerTokenAuthenticationToken(accessToken)
    }

    override fun extractAccessToken(request: HttpServletRequest): String? =
        MyJwtTokenExtractor.extractAccessTokenFromHeader(request)
}
