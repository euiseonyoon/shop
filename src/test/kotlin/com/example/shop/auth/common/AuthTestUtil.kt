package com.example.shop.auth.common

import com.example.shop.constants.REFRESH_TOKEN_KEY
import com.example.shop.auth.TestConstants.Companion.TEST_EMAIL
import com.example.shop.auth.TestConstants.Companion.TEST_PSWD
import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.models.EmailPasswordLoginRequest
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.security.user_services.OauthAuthenticatedUserAutoRegisterer
import com.example.shop.auth.services.AccountService
import com.example.shop.common.response.GlobalResponse
import com.example.shop.redis.tokens.repositories.RefreshTokenRedisRepository
import com.example.shop.constants.EMAIL_PASSWORD_AUTH_URI
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AuthTestUtil {
    companion object {
        fun makePostCall(
            mockMvc: MockMvc,
            uri: String,
            requestBodyJson: String?,
            headersMap: Map<String, String>?,
        ): ResultActions {
            val requestBuilder = post(uri)
                .contentType(MediaType.APPLICATION_JSON)

            requestBodyJson?.let {
                requestBuilder.content(it)
            }

            headersMap?.let {
                headersMap.forEach { (header, value) ->
                    requestBuilder.header(header, value)
                }
            }

            return mockMvc.perform(requestBuilder)
        }

        fun checkAccessTokenAndRefreshTokenExistence(
            mockMvc: MockMvc,
            uri: String,
            loginRequestJson: String,
        ): MvcResult {
            return makePostCall(mockMvc, uri, loginRequestJson, null)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.accessToken").exists())
            .andExpect(cookie().exists(REFRESH_TOKEN_KEY))
            .andReturn()
        }

        fun checkAccessTokenAgainstDb(
            mvcResult: MvcResult,
            json: Json,
            myJwtTokenHelper: MyJwtTokenHelper,
            accountService: AccountService,
        ) {
            // THEN
            val responseBody = mvcResult.response.contentAsString
            val loginResponse = json.decodeFromString<GlobalResponse<TokenResponse>>(responseBody)
            assertFalse(loginResponse.isError)
            assertNull(loginResponse.errorMsg)
            assertNotNull(loginResponse.result?.accessToken)

            val accessToken = loginResponse.result!!.accessToken

            // THEN: 정상 parsing 되는지 확인
            val claims = assertDoesNotThrow {
                myJwtTokenHelper.parseAccessToken(accessToken)
            }

            // THEN: Db로 부터 account 조회되는지 확인
            val accountId = myJwtTokenHelper.getSubject(claims)
            val foundAccount = accountService.findWithAuthoritiesById(accountId)
            assertNotNull(foundAccount)

            // THEN: Role + 그룹 권한 같은지 확인
            val authoritiesFromAccessToken = myJwtTokenHelper.getAuthorityStringList(claims).toSet()
            val authoritiesFromDb = foundAccount.groupAuthorities.mapNotNull { it.name }.toMutableSet().also {
                it.add(foundAccount.authority.roleName!!)
            }
            assertEquals(authoritiesFromDb, authoritiesFromAccessToken)
        }

        fun checkRefreshTokenFromCookie(
            mvcResult: MvcResult,
            myJwtTokenHelper: MyJwtTokenHelper,
            accountService: AccountService,
            refreshTokenRedisRepository: RefreshTokenRedisRepository
        ) {
            val issuedRefreshToken = mvcResult.response.cookies.find { it ->
                it.name == REFRESH_TOKEN_KEY
            }
            assertNotNull(issuedRefreshToken)

            // THEN: 정상 parsing 되는지 확인
            val claims = assertDoesNotThrow {
                myJwtTokenHelper.parseRefreshToken(issuedRefreshToken.value)
            }

            // THEN: Db로 부터 account 조회되는지 확인
            val accountId = myJwtTokenHelper.getSubject(claims)
            val foundAccount = accountService.findWithAuthoritiesById(accountId)
            assertNotNull(foundAccount)

            val refreshKeyOnRedis = refreshTokenRedisRepository.find(accountId)
            assertEquals(issuedRefreshToken.value, refreshKeyOnRedis!!)
        }

        fun getLogInResult(
            mockMvc: MockMvc,
            oauthAuthenticatedUserAutoRegisterer: OauthAuthenticatedUserAutoRegisterer,
            json: Json
        ): MvcResult {
            val accountCreationResult = oauthAuthenticatedUserAutoRegisterer.findOrCreateUser(
                TEST_EMAIL,
                ThirdPartyAuthenticationVendor.GOOGLE,
            )
            val loginRequestJson = json.encodeToString(
                EmailPasswordLoginRequest.serializer(),
                EmailPasswordLoginRequest(email = TEST_EMAIL, password = TEST_PSWD)
            )
            return makePostCall(mockMvc, EMAIL_PASSWORD_AUTH_URI, loginRequestJson, null).andReturn()
        }
    }
}
