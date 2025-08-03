package com.example.shop.auth.security.filters.authentication_converter

import com.example.shop.auth.security.utils.MyJwtTokenExtractor
import com.example.shop.auth.security.utils.MyJwtTokenExtractorImpl
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.stereotype.Component

@Component
class CustomJwtAuthenticationConverter(
    override val myJwtTokenExtractor: MyJwtTokenExtractor
) : AuthenticationConverter, MyJwtAuthenticationConverter {
    override fun convert(request: HttpServletRequest): Authentication? {
        val accessToken = extractAccessToken(request) ?: return null
        return BearerTokenAuthenticationToken(accessToken)
    }
}
