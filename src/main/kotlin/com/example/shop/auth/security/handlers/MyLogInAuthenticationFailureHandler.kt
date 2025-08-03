package com.example.shop.auth.security.handlers

import com.example.shop.auth.models.TokenResponse
import com.example.shop.common.apis.GlobalResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.json.Json
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

class MyLogInAuthenticationFailureHandler(
    private val json: Json,
) : AuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException?
    ) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        // 응답 Content-Type을 JSON으로 설정
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val globalResponse = GlobalResponse.createErrorRes<TokenResponse>(
            response = response,
            errorMsg = "Authentication failed",
            status = HttpStatus.UNAUTHORIZED
        )
        val jsonResponse = json.encodeToString(GlobalResponse.serializer(TokenResponse.serializer()), globalResponse)

        response.writer.write(jsonResponse)
        response.writer.flush()
    }
}
