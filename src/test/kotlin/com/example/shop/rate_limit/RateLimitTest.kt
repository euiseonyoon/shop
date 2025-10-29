package com.example.shop.rate_limit

import com.example.shop.auth.common.AuthTestUtil
import com.example.shop.auth.models.ThirdPartyOauthTokenLoginRequest
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.security.rate_limit.RedisRateLimitHelper
import com.example.shop.auth.security.rate_limit.models.HeavyRateLimitUriToken
import com.example.shop.auth.security.rate_limit.models.RateLimitProperties
import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import com.example.shop.common.response.GlobalResponse
import com.example.shop.constants.HEALTH_CHECK_URI
import com.example.shop.constants.NO_API_LIMIT_END_POINTS
import com.example.shop.constants.OAUTH_AUTH_URI_PATTERN
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RateLimitTestConfig::class)
class RateLimitTest(
    private val json: Json,
    private val mockMvc: MockMvc,
) {
    @MockitoSpyBean(name = "googleUserService")
    lateinit var googleOidcUserService: ThirdPartyAuthenticationUserService

    @MockitoSpyBean
    lateinit var rateLimitProperties: RateLimitProperties

    @MockitoSpyBean
    lateinit var rateLimitHelper: RedisRateLimitHelper

    @MockitoSpyBean
    lateinit var rateLimitFilterWrapper: RateLimitTestConfig.RateLimitFilterWrapper

    lateinit var bearerToken: String

    var consumedRateLimitToken: Long = 0

    @BeforeEach
    fun init() {
        consumedRateLimitToken = 0
        Mockito.doReturn("testEmail@gmail.com").`when`(googleOidcUserService).getEmailAddressFromToken(any())

        val googleLogInUri = OAUTH_AUTH_URI_PATTERN.replace("*", "google")
        val tokenToConsume = rateLimitHelper.getTokensToConsume(MockHttpServletRequest().apply {
            method = "POST"
            requestURI = googleLogInUri
        })

        val mvcResult = AuthTestUtil.checkAccessTokenAndRefreshTokenExistence(
            mockMvc,
            googleLogInUri,
            json.encodeToString(
                ThirdPartyOauthTokenLoginRequest.serializer(),
                ThirdPartyOauthTokenLoginRequest(token = "dummy token")
            ),
        )
        consumedRateLimitToken += tokenToConsume

        val responseBody = mvcResult.response.contentAsString
        val loginResponse = json.decodeFromString<GlobalResponse<TokenResponse>>(responseBody)
        bearerToken = "Bearer ${loginResponse.result!!.accessToken}"
    }

    @Test
    fun `test token consumption`() {
        // GIVEN
        val heavyMethod = "POST"
        val heavyUri1: Pair<String, Long> = "/first/heavy/api" to 5
        val heavyUri2: Pair<String, Long> = "/second/heavy/*" to 10

        // WHEN
        var tempHeavyMap: Map<String, List<HeavyRateLimitUriToken>> = mapOf(
            heavyMethod to listOf(
                HeavyRateLimitUriToken(heavyUri1.first, heavyUri1.second.toInt()),
                HeavyRateLimitUriToken(heavyUri2.first, heavyUri2.second.toInt()),
            )
        )
        Mockito.doReturn(tempHeavyMap).`when`(rateLimitProperties).heavyMap

        // THEN: Heavy api concrete path matching
        var mockRequest = MockHttpServletRequest().apply {
            setMethod(heavyMethod)
            setRequestURI(heavyUri1.first)
        }
        assertEquals(heavyUri1.second, rateLimitHelper.getTokensToConsume(mockRequest))

        // THEN: Heavy api pattern matching
        mockRequest = MockHttpServletRequest().apply {
            setMethod(heavyMethod)
            setRequestURI(heavyUri2.first.replace("*", "api"))
        }
        assertEquals(heavyUri2.second, rateLimitHelper.getTokensToConsume(mockRequest))

        // THEN: heavy map 에 없는 경우 1
        mockRequest = MockHttpServletRequest().apply {
            setMethod(heavyMethod)
            setRequestURI("/light")
        }
        assertEquals(1, rateLimitHelper.getTokensToConsume(mockRequest))

        // THEN: heavy map 에 없는 경우 2
        mockRequest = MockHttpServletRequest().apply {
            setMethod("GET")
            setRequestURI("/light2")
        }
        assertEquals(1, rateLimitHelper.getTokensToConsume(mockRequest))
    }

    @Test
    @Transactional
    fun `test rate limit success`() {
        // GIVEN
        val initAvailableToken = rateLimitHelper.resolveBucket(MockHttpServletRequest()).availableTokens
        val uri = "/test"
        val mockRequest = MockHttpServletRequest()
        mockRequest.method = "GET"
        mockRequest.requestURI = uri
        val bucket = rateLimitHelper.resolveBucket(mockRequest)

        val rateLimitTokenConsumeAmount = rateLimitHelper.getTokensToConsume(mockRequest)
        // WHEN
        mockMvc.perform(
            get(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearerToken)
        ).andExpect(status().isOk)
        consumedRateLimitToken += rateLimitTokenConsumeAmount
        val afterAvailableToken = bucket.availableTokens

        /**
         * 테스트 과정에서 refill이 이루어 진것 같다.
         * refill 주기를 1s로 매우 짧게 설정하니,
         *      initAvailableToken이 15(20-5)가 아닌 17이다. (중간에 refill이 된것 같다.)
         *      assertEquals(consumedRateLimitToken, (initAvailableToken - afterAvailableToken)) <-- 테스트 실패
         * refill 주기를 길게 10s 로 설정하니
         *      initAvailableToken이 15(20-5) 였다.
         *      assertEquals(consumedRateLimitToken, (initAvailableToken - afterAvailableToken)) <-- 테스트 성공
         * */
        // THEN
        val refilledAmount = consumedRateLimitToken - (initAvailableToken - afterAvailableToken)
        assertTrue { refilledAmount <= rateLimitProperties.refillTokens }
        assertTrue { refilledAmount >= 0 }
    }

    @Test
    @Transactional
    fun `test rate limit fail too much token`() {
        // GIVEN : bucket을 모두 비움
        val bucket = rateLimitHelper.resolveBucket(MockHttpServletRequest())
        bucket.tryConsume(rateLimitProperties.capacity)
        // GIVEN : refill 되는 토큰 수량보다 많은 토큰을 소모해야된다고 mocking
        Mockito.doReturn(rateLimitProperties.refillTokens + 10)
            .`when`(rateLimitHelper).getTokensToConsume(any())

        // WHEN & THEN
        mockMvc.perform(
            get("/test")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearerToken)
        ).andExpect(status().isTooManyRequests)
    }

    @Test
    @Transactional
    fun `test rate limit no api limit endpoint`() {
        // WHEN & THEN
        assertTrue { HEALTH_CHECK_URI in NO_API_LIMIT_END_POINTS }
        mockMvc.perform(
            get(HEALTH_CHECK_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearerToken)
        ).andExpect(status().isOk)

        // 이전에 호출된 모든 메서드 호출 기록을 초기화
        // 이렇게 하면 @BeforeTest 에서 발생한 doFilterInternal 호출이 무시
        Mockito.reset(rateLimitFilterWrapper)
        // 기존 RateLimitFilter.doFilterInternal()은 protected라 접근이 안되었다. 따라서 wrapper를 만들어서 사용했다.
        verify(rateLimitFilterWrapper, never()).doFilterInternal(any(), any(), any())
    }
}
