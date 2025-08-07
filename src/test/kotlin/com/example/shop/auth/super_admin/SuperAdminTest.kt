package com.example.shop.auth.super_admin

import com.example.shop.constants.ADMIN_NAME
import com.example.shop.constants.SUPER_ADMIN_NAME
import com.example.shop.auth.TestConstants.Companion.TEST_PSWD
import com.example.shop.auth.common.AuthTestUtil
import com.example.shop.auth.login_test.email_password.TestPasswordGenerator
import com.example.shop.auth.models.ThirdPartyOauthTokenLoginRequest
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import com.example.shop.auth.security.utils.PasswordGenerator
import com.example.shop.auth.services.AccountService
import com.example.shop.common.apis.GlobalResponse
import com.example.shop.constants.ADMIN_URI_PREFIX
import com.example.shop.constants.OAUTH_AUTH_URI_PATTERN
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.convention.TestBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertNotEquals

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SuperAdminTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var json: Json

    @TestBean
    lateinit var passwordGenerator: PasswordGenerator

    @MockitoSpyBean(name = "googleUserService")
    private lateinit var googleOidcUserService: ThirdPartyAuthenticationUserService

    @Value("\${auth.super_admin}")
    lateinit var adminEmail: String

    @Autowired
    lateinit var accountService: AccountService

    companion object {
        @JvmStatic
        fun passwordGenerator(): PasswordGenerator {
            return TestPasswordGenerator(fixedPassword = TEST_PSWD)
        }
    }



    @Test
    @Transactional
    fun `test super admin`() {
        // GIVEN
        Mockito.doReturn(adminEmail).`when`(googleOidcUserService).getEmail(any())

        val mvcResult = AuthTestUtil.checkAccessTokenAndRefreshTokenExistence(
            mockMvc,
            OAUTH_AUTH_URI_PATTERN.replace("*", "google"),
            json.encodeToString(
                ThirdPartyOauthTokenLoginRequest.serializer(),
                ThirdPartyOauthTokenLoginRequest(token = "dummy token")
            ),
        )
        val adminAccount = accountService.findWithAuthoritiesByEmail(adminEmail)
        assertNotNull(adminAccount)
        // check account user is NOT created as SUPER ADMIN or ADMIN
        assertNotEquals(SUPER_ADMIN_NAME, adminAccount.authority!!.roleName!!)
        assertNotEquals(ADMIN_NAME, adminAccount.authority!!.roleName!!)

        val responseBody = mvcResult.response.contentAsString
        val loginResponse = json.decodeFromString<GlobalResponse<TokenResponse>>(responseBody)
        val bearerToken = "Bearer ${loginResponse.result!!.accessToken}"

        // WHEN & THEN
        mockMvc.perform(
            get("$ADMIN_URI_PREFIX/test")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearerToken)
        ).andExpect(status().isOk)
    }

}
