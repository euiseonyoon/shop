package com.example.shop.security.filters

import com.example.shop.security.models.ThirdPartyOauthTokenLoginRequest
import com.example.shop.security.models.ThirdPartyOauthAuthenticationToken
import com.example.shop.security.third_party_auth.enums.ThirdPartyAuthenticationVendor
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.json.Json
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import java.io.IOException

class ThirdPartyOauthAuthenticationFilter(
    targetUri: String,
    authenticationManager: AuthenticationManager,
) : AbstractAuthenticationProcessingFilter(targetUri, authenticationManager) {

    private val json = Json { ignoreUnknownKeys = true }

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Authentication {
        // HTTP 요청 메서드가 POST인지 확인합니다.
        if (request.method != "POST") {
            // POST가 아니면 인증 예외를 발생시킵니다.
            throw AuthenticationServiceException("Authentication method not supported: ${request.method}")
        }

        try {
            // 요청 URI에서 벤더 ID를 추출
            // 예: /login/oauth/google -> "google"
            val requestUri = request.requestURI
            val pathSegments = requestUri.split("/")
            val vendor = ThirdPartyAuthenticationVendor.fromString(
                pathSegments.getOrNull(3)
                    ?: throw AuthenticationServiceException("Missing provider ID in URL: $requestUri")
            )

            // inputStream은 한번만 읽을 수 있지만, 일단 여기서는 한번만 사용하면 되니 ContentCachingRequestWrapper건 일단 생략
            val requestBodyString = request.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val loginRequest = json.decodeFromString<ThirdPartyOauthTokenLoginRequest>(requestBodyString)
            val authRequest = ThirdPartyOauthAuthenticationToken(loginRequest.token, vendor, emptyList())
            return this.authenticationManager.authenticate(authRequest)
        } catch (e: IOException) {
            throw AuthenticationServiceException("Failed to parse authentication request.", e)
        }
    }
}
