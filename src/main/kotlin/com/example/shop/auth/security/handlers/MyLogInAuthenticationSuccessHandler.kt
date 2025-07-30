package com.example.shop.auth.security.handlers

import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

class MyLogInAuthenticationSuccessHandler(
    private val jwtHelper: MyJwtTokenHelper,
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val email = if (authentication.principal is UserDetails) {
            (authentication.principal as UserDetails).username
        } else {
            (authentication.principal as OAuth2User).name
        }

        val accessToken = jwtHelper.createAccessToken(email, authentication)
        val refreshToken = jwtHelper.createRefreshToken(email)

        val refreshTokenCookie = Cookie("refreshToken", refreshToken).apply {
            isHttpOnly = true // JavaScript 접근 방지
            path = "/" // 모든 경로에서 쿠키 사용 가능
            maxAge = (jwtHelper.refreshTokenExpirationMs / 1000).toInt() // Refresh Token 유효 기간 (초 단위)
            secure = true
        }
        response.addCookie(refreshTokenCookie)

        // 응답 본문에 토큰을 JSON 형식으로 작성합니다.
        response.status = HttpStatus.OK.value() // HTTP 상태 코드를 200 OK로 설정
        response.contentType = MediaType.APPLICATION_JSON_VALUE // 응답 Content-Type을 JSON으로 설정
        response.writer.write(
            """
            {
                "message": "Login successful",
                "accessToken": "$accessToken"
            }
            """.trimIndent()
        )
        response.writer.flush() // 응답 버퍼를 비웁니다.
    }
}
