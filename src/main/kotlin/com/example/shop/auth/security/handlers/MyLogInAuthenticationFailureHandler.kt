package com.example.shop.auth.security.handlers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

class MyLogInAuthenticationFailureHandler : AuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException?
    ) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        // 응답 Content-Type을 JSON으로 설정
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        // 응답 본문에 에러 메시지를 JSON 형식으로 작성합니다.
        response.writer.write(
            """
            {
                "message": "Authentication failed",
                "exception": ${exception?.message}
            }
            """.trimIndent()
        )
        response.writer.flush()
    }
}
