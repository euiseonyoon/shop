package com.example.shop.auth.jwt_auth_test

import com.example.shop.auth.TestConstants.Companion.TEST_PSWD
import com.example.shop.auth.common.AuthTestUtil
import com.example.shop.auth.login_test.email_password.TestPasswordGenerator
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.security.user_services.OauthAuthenticatedUserAutoRegisterer
import com.example.shop.auth.security.utils.PasswordGenerator
import com.example.shop.common.response.GlobalResponse
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.convention.TestBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtAuthenticationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var json: Json

    @TestBean
    lateinit var passwordGenerator: PasswordGenerator

    @Autowired
    lateinit var oauthAuthenticatedUserAutoRegisterer: OauthAuthenticatedUserAutoRegisterer

    companion object {
        @JvmStatic
        fun passwordGenerator(): PasswordGenerator {
            return TestPasswordGenerator(fixedPassword = TEST_PSWD)
        }
    }

    private fun makeUserAccountAndGetAccessToken(): String {
        val mvcResult = AuthTestUtil.getLogInResult(mockMvc, oauthAuthenticatedUserAutoRegisterer, json)
        val responseBody = mvcResult.response.contentAsString
        val loginResponse = json.decodeFromString<GlobalResponse<TokenResponse>>(responseBody)
        return loginResponse.result!!.accessToken.let {
            "Bearer $it"
        }
    }

    @Test
    @Transactional
    fun `test jwt token authentication`() {
        // GIVEN
        val bearerToken = makeUserAccountAndGetAccessToken()

        // WHEN & THEN
        mockMvc.perform(
            get("/test")
                .header("Authorization", bearerToken)
        ).andExpect(status().isOk)
    }

    @Test
    @Transactional
    fun `test jwt token authentication user allowed method`() {
        // GIVEN
        val bearerToken = makeUserAccountAndGetAccessToken()

        // WHEN & THEN
        mockMvc.perform(
            get("/test-user")
                .header("Authorization", bearerToken)
        ).andExpect(status().isOk)
    }

    @Test
    @Transactional
    fun `test jwt token authentication call admin only method`() {
        // GIVEN
        val bearerToken = makeUserAccountAndGetAccessToken()

        // WHEN & THEN
        mockMvc.perform(
            get("/test-admin-only")
                .header("Authorization", bearerToken)
        ).andExpect(status().isForbidden)
    }

    @Test
    @Transactional
    fun `test jwt token authentication expired access token`() {
        val expiredAccessToken = "yJhbGciOiJIUzI1NiJ9.eyJhdXRoIjoiUk9MRV9VU0VSIiwic3ViIjoi" +
                "MzAwMiIsImlhdCI6MTc1NDIxOTU3MSwiaXNzIjoibXktc2hvcC1kZW1vLXByb2plY3QiLCJleH" +
                "AiOjE3NTQyMjEzNzF9.v6I9WcwF8zKFB-Y_HcF06E4OvBDFE9-BPklYDpWZSns"

        val bearerToken = "Bearer $expiredAccessToken"

        mockMvc.perform(
            get("/test")
                .header("Authorization", bearerToken)
        ).andExpect(status().isForbidden)
    }
}
