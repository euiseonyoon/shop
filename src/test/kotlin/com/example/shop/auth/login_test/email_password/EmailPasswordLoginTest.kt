package com.example.shop.auth.login_test.email_password

import com.example.shop.auth.TestConstants.Companion.TEST_EMAIL
import com.example.shop.auth.TestConstants.Companion.TEST_PSWD
import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.common.AuthTestUtil
import com.example.shop.auth.domain.Email
import com.example.shop.auth.models.EmailPasswordLoginRequest
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.security.user_services.OauthAuthenticatedUserAutoRegisterer
import com.example.shop.auth.security.utils.PasswordGenerator
import com.example.shop.auth.services.AccountDomainService
import com.example.shop.auth.services.AccountService
import com.example.shop.cart.services.CartDomainService
import com.example.shop.constants.EMAIL_PASSWORD_AUTH_URI
import com.example.shop.redis.tokens.repositories.RefreshTokenRedisRepository
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.convention.TestBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional


@SpringBootTest
@AutoConfigureMockMvc
class EmailPasswordLoginTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var json: Json

    @Autowired
    lateinit var myJwtTokenHelper: MyJwtTokenHelper

    @Autowired
    lateinit var accountDomainService: AccountDomainService

    @Autowired
    private lateinit var oauthAuthenticatedUserAutoRegisterer: OauthAuthenticatedUserAutoRegisterer

    @Autowired
    lateinit var refreshTokenRedisRepository: RefreshTokenRedisRepository

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
    fun `test email password login`() {
        // GIVEN
        val accountCreationResult = oauthAuthenticatedUserAutoRegisterer.findOrCreateUser(
            Email(TEST_EMAIL),
            ThirdPartyAuthenticationVendor.GOOGLE,
        )
        val loginRequestJson = json.encodeToString(
            EmailPasswordLoginRequest.serializer(),
            EmailPasswordLoginRequest(email = TEST_EMAIL, password = TEST_PSWD),
        )

        // THEN
        val mvcResult = AuthTestUtil.checkAccessTokenAndRefreshTokenExistence(
            mockMvc,
            EMAIL_PASSWORD_AUTH_URI,
            loginRequestJson,
        )
        // THEN
        AuthTestUtil.checkAccessTokenAgainstDb(mvcResult, json, myJwtTokenHelper, accountDomainService)
        AuthTestUtil.checkRefreshTokenFromCookie(
            mvcResult,
            myJwtTokenHelper,
            accountDomainService,
            refreshTokenRedisRepository,
        )
    }

    @Test
    @Transactional
    fun `test email password login wrong password`() {
        // GIVEN
        oauthAuthenticatedUserAutoRegisterer.findOrCreateUser(Email(TEST_EMAIL), ThirdPartyAuthenticationVendor.GOOGLE)
        val loginRequestJson = json.encodeToString(
            EmailPasswordLoginRequest.serializer(),
            EmailPasswordLoginRequest(email = TEST_EMAIL, password = "wrong password")
        )

        // WHEN & THEN
        AuthTestUtil
            .makePostCall(mockMvc, EMAIL_PASSWORD_AUTH_URI, loginRequestJson, null)
            .andExpect(status().isUnauthorized)
    }
}
