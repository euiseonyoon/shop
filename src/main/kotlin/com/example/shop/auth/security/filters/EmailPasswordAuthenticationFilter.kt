package com.example.shop.auth.security.filters

import com.example.shop.auth.security.SecurityConfig.Companion.EMAIL_PASSWORD_AUTH_URI
import com.example.shop.auth.models.EmailPasswordLoginRequest
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.json.Json
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.IOException

class EmailPasswordAuthenticationFilter(
    authenticationManager: AuthenticationManager,
) : UsernamePasswordAuthenticationFilter(authenticationManager) {

    private val json = Json { ignoreUnknownKeys = true }

    init {
        setFilterProcessesUrl(EMAIL_PASSWORD_AUTH_URI)
    }

    // 로그인 요청을 가로채서 이메일/비밀번호를 추출하고 인증 토큰을 생성합니다.
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        // HTTP 요청 메서드가 POST인지 확인합니다.
        if (request.method != "POST") {
            // POST가 아니면 인증 예외를 발생시킵니다.
            throw AuthenticationServiceException("Authentication method not supported: ${request.method}")
        }

        try {
            // inputStream은 한번만 읽을 수 있지만, 일단 여기서는 한번만 사용하면 되니 ContentCachingRequestWrapper건 일단 생략
            val requestBodyString = request.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val loginRequest = json.decodeFromString<EmailPasswordLoginRequest>(requestBodyString)
            val authRequest = UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password)
            return this.authenticationManager.authenticate(authRequest)
        } catch (e: IOException) {
            throw AuthenticationServiceException("Failed to parse authentication request body", e)
        }
    }
}
