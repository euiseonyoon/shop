package com.example.shop.common

import com.example.shop.auth.common.AuthTestUtil
import com.example.shop.auth.models.ThirdPartyOauthTokenLoginRequest
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.services.AccountService
import com.example.shop.common.response.GlobalResponse
import com.example.shop.constants.OAUTH_AUTH_URI_PATTERN
import kotlinx.serialization.json.Json
import org.springframework.test.web.servlet.MockMvc

class AccessTokenGetter(
    private val accountService: AccountService,
    private val mockMvc: MockMvc,
    private val json: Json,
) {
    fun getBearerToken(email: String): String {
        val mvcResult = AuthTestUtil.checkAccessTokenAndRefreshTokenExistence(
            mockMvc,
            OAUTH_AUTH_URI_PATTERN.replace("*", "google"),
            json.encodeToString(
                ThirdPartyOauthTokenLoginRequest.serializer(),
                ThirdPartyOauthTokenLoginRequest(token = "dummy token")
            ),
        )
        val responseBody = mvcResult.response.contentAsString
        val loginResponse = json.decodeFromString<GlobalResponse<TokenResponse>>(responseBody)
        return "Bearer ${loginResponse.result!!.accessToken}"
    }
}
