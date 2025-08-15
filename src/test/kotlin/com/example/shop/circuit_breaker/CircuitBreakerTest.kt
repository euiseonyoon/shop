package com.example.shop.circuit_breaker

import com.example.shop.auth.TestConstants.Companion.TEST_EMAIL
import com.example.shop.auth.common.AuthTestUtil
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import com.example.shop.auth.security.user_services.OauthAuthenticatedUserAutoRegisterer
import com.example.shop.common.logger.LogSupport
import com.example.shop.common.response.GlobalResponse
import com.example.shop.constants.REFRESH_TOKEN_KEY
import com.example.shop.redis.tokens.repositories.RefreshTokenRedisRepository
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.isNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.MockMvc
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CircuitBreakerTest : LogSupport() {
    // 1.1. login -> refresh token 발급 & 저장
    // 1.2. auth/token/refresh -> refresh token 발급 & 저장
    // 2. rate limit 에서 확인
    // 3. authority 생성 / 변경 -> redis를 통한 pub/sub
    // private val redisContainer = TestRedisContainerConfig.redisContainer

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var oauthAuthenticatedUserAutoRegisterer: OauthAuthenticatedUserAutoRegisterer

    @Autowired
    lateinit var json: Json

    @MockitoSpyBean(name = "googleUserService")
    private lateinit var googleOidcUserService: ThirdPartyAuthenticationUserService

    @MockitoSpyBean
    private lateinit var refreshTokenRedisRepository: RefreshTokenRedisRepository

    @Autowired
    @Qualifier("redisCircuitBreaker")
    private lateinit var redisCircuitBreaker: CircuitBreaker

    @Test
    fun `test login refresh token redis circuit breaker`() {
        // GIVEN
        Mockito.doReturn(TEST_EMAIL).`when`(googleOidcUserService).getEmail(any())
        Mockito.doThrow(RuntimeException("Redis save failed.")).`when`(refreshTokenRedisRepository).save(any(), any(), isNull())

        val slidingWindowSize = redisCircuitBreaker.circuitBreakerConfig.slidingWindowSize
        (1..slidingWindowSize).map { index ->
            val mvcResult = AuthTestUtil.getLogInResult(mockMvc, oauthAuthenticatedUserAutoRegisterer, json)
            val responseBody = mvcResult.response.contentAsString
            val refreshTokenCookie = mvcResult.response.cookies.find { it.name == REFRESH_TOKEN_KEY }
            val loginResponse = json.decodeFromString<GlobalResponse<TokenResponse>>(responseBody)

            // access token은 response body에 포함
            assertNotNull(loginResponse.result?.accessToken)
            // redis 오류로 인하여, refresh token은 쿠키에 포함되지 않음
            assertNull(refreshTokenCookie)

            if (index < slidingWindowSize) {
                assertEquals(CircuitBreaker.State.CLOSED, redisCircuitBreaker.state)
            } else {
                assertEquals(CircuitBreaker.State.OPEN, redisCircuitBreaker.state)
            }
        }
    }

}
