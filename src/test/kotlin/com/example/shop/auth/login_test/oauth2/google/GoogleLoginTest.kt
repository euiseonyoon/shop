package com.example.shop.auth.login_test.oauth2.google

import com.example.shop.auth.TestConstants.Companion.TEST_EMAIL
import com.example.shop.auth.TestConstants.Companion.TEST_PSWD
import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.common.AuthTestUtil
import com.example.shop.auth.login_test.email_password.TestPasswordGenerator
import com.example.shop.auth.models.EmailPasswordLoginRequest
import com.example.shop.auth.models.ThirdPartyOauthTokenLoginRequest
import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import com.example.shop.auth.security.utils.PasswordGenerator
import com.example.shop.auth.services.AccountService
import com.example.shop.redis.tokens.repositories.RefreshTokenRedisRepository
import com.example.shop.constants.EMAIL_PASSWORD_AUTH_URI
import com.example.shop.constants.OAUTH_AUTH_URI_PATTERN
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.convention.TestBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional


@SpringBootTest
@AutoConfigureMockMvc
class GoogleLoginTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var json: Json

    @Autowired
    private lateinit var myJwtTokenHelper: MyJwtTokenHelper

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    lateinit var refreshTokenRedisRepository: RefreshTokenRedisRepository

    @MockitoSpyBean(name = "googleUserService")
    private lateinit var googleOidcUserService: ThirdPartyAuthenticationUserService

    @TestBean
    lateinit var passwordGenerator: PasswordGenerator

    companion object {
        @JvmStatic
        fun passwordGenerator(): PasswordGenerator {
            return TestPasswordGenerator(fixedPassword = TEST_PSWD)
        }
    }

    @Test
    @Transactional
    fun `test google oidc login`() {
        // GIVEN
        Mockito.doReturn(TEST_EMAIL).`when`(googleOidcUserService).getEmail(any())

        // WHEN & THEN
        val mvcResult = AuthTestUtil.checkAccessTokenAndRefreshTokenExistence(
            mockMvc,
            OAUTH_AUTH_URI_PATTERN.replace("*", "google"),
            json.encodeToString(
                ThirdPartyOauthTokenLoginRequest.serializer(),
                ThirdPartyOauthTokenLoginRequest(token = "dummy token")
            ),
        )
        // THEN
        AuthTestUtil.checkAccessTokenAgainstDb(mvcResult, json, myJwtTokenHelper, accountService)
        AuthTestUtil.checkRefreshTokenFromCookie(
            mvcResult,
            myJwtTokenHelper,
            accountService,
            refreshTokenRedisRepository,
        )

        // THEN: 임시로 만들어진 password로 로그인 되는지 확인
        val emailPasswordResult = AuthTestUtil.checkAccessTokenAndRefreshTokenExistence(
            mockMvc,
            EMAIL_PASSWORD_AUTH_URI,
            json.encodeToString(
                EmailPasswordLoginRequest.serializer(),
                EmailPasswordLoginRequest(TEST_EMAIL, TEST_PSWD),
            ),
        )
    }

    @Test
    @Transactional
    fun `test google oidc login expired id token`() {
        // WHEN
        val expiredGoogleIdToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImRkNTMwMTIwNGZjMWQ2YTBkNjhjNzgzYTM1Y2M5YzEw" +
                "YjI1ZTFmNGEiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI0" +
                "MzIyNDUzMzcxNTQtNTJ0dDR0M2pyMGZ0ajhtYzQ2dmp0bGFtbmI5bTg3NHQuYXBwcy5nb29nbGV1c2VyY29udGVudC5" +
                "jb20iLCJhdWQiOiI0MzIyNDUzMzcxNTQtNTJ0dDR0M2pyMGZ0ajhtYzQ2dmp0bGFtbmI5bTg3NHQuYXBwcy5nb29nbG" +
                "V1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDA2NTg5MjcwNjAxMTQ3MTc3NjIiLCJlbWFpbCI6ImRhbmNlZHJhZ29uM" +
                "jVAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5iZiI6MTc1NDIxOTIyNCwibmFtZSI6IkxFRSBZT08i" +
                "LCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jS1FoZE1zMjBjU0RhNlF" +
                "CUzEzR0RJbU5LU3NzaS0tLTVFVzVTYnB3Ykx5ODFROUhRPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6IkxFRSIsImZhbWlseV" +
                "9uYW1lIjoiWU9PIiwiaWF0IjoxNzU0MjE5NTI0LCJleHAiOjE3NTQyMjMxMjQsImp0aSI6IjBkNTAwOWQyYjFjYmI4Y" +
                "zE2MzYxZDVkZjQ2ZGYwYmVjMTlhNjExMDkifQ.gB6kqf1l6IQq73SqjB4ftAbLPr0DJvLYmZC9ozUz5O43dg0nIUR_Y" +
                "e1oMdbDJk7zG2RcEHG7O_HhySuWr3MGUnBBqyPWuI5RlyiW9RI2hQzfY1qBBjPUH2XGAHP1ri8sOIKTUYL9z_PHyIvZ" +
                "IyFQ3NXz3LqFFzkYISA34BN1AXuFnTRGFIYs5wswLS9xG9iBdHQe0JbZnihKIHXsQkVIkOfLSBJ3E1aGkRPHIfqvj8Tl" +
                "72nfPJz1lenvKfPu0Hc7fMs9pPUIIkNSRkxaOFuc8jk4kKhccZhtj5QeIywZ_H4q_OjTa1GYar3Ckw_lthSKspxFQqM9" +
                "EUfxVIPuXMgF3Q"

        val inputJsonRequest = json.encodeToString(
            ThirdPartyOauthTokenLoginRequest.serializer(),
            ThirdPartyOauthTokenLoginRequest(token = expiredGoogleIdToken)
        )

        AuthTestUtil.makePostCall(
            mockMvc,
            OAUTH_AUTH_URI_PATTERN.replace("*", "google"),
            inputJsonRequest,
            null,
        ).andExpect(status().isUnauthorized)
    }
}
