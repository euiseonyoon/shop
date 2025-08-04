package com.example.shop.auth.token_refresh

import com.example.shop.auth.REFRESH_TOKEN_KEY
import com.example.shop.auth.TOKEN_REFRESH_URI
import com.example.shop.auth.common.AuthTestUtil
import com.example.shop.auth.exceptions.BadRefreshTokenStateException
import com.example.shop.auth.security.user_services.OauthAuthenticatedUserAutoRegisterer
import com.example.shop.auth.utils.RefreshTokenStateHelper
import jakarta.servlet.http.Cookie
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertNotEquals

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TokenRefreshConfig::class)
class TokenRefreshTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var oauthAuthenticatedUserAutoRegisterer: OauthAuthenticatedUserAutoRegisterer

    @MockitoSpyBean
    lateinit var refreshTokenStateHelper: RefreshTokenStateHelper

    @Autowired
    lateinit var json: Json

    @Test
    @Transactional
    fun `test token refresh`() {
        // GIVEN
        val mvcResult = AuthTestUtil.getLogInResult(mockMvc, oauthAuthenticatedUserAutoRegisterer, json)
        val issuedRefreshToken = mvcResult.response.cookies.find {
            it.name == REFRESH_TOKEN_KEY
        }!!.value

        // WHEN
        val refreshTokenCookie = Cookie(REFRESH_TOKEN_KEY, issuedRefreshToken)
        val newRefreshTokenResult = mockMvc.perform(
            post(TOKEN_REFRESH_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(refreshTokenCookie)
        ).andReturn()
        val newRefreshToken = newRefreshTokenResult.response.cookies.find {
            it.name == REFRESH_TOKEN_KEY
        }!!.value

        // THEN
        assertNotEquals(issuedRefreshToken, newRefreshToken)
    }

    @Test
    @Transactional
    fun `test token refresh redis validation fail`() {
        // GIVEN
        Mockito.doThrow(BadRefreshTokenStateException("bad refresh token"))
            .`when`(refreshTokenStateHelper).validateRefreshToken(any(), any())
        val mvcResult = AuthTestUtil.getLogInResult(mockMvc, oauthAuthenticatedUserAutoRegisterer, json)
        val issuedRefreshToken = mvcResult.response.cookies.find {
            it.name == REFRESH_TOKEN_KEY
        }!!.value

        // WHEN
        val refreshTokenCookie = Cookie(REFRESH_TOKEN_KEY, issuedRefreshToken)
        val resultActions: ResultActions = mockMvc.perform(
            post(TOKEN_REFRESH_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(refreshTokenCookie)
        )

        // THEN
        resultActions.andExpect(status().isBadRequest)
    }
}
