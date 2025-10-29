package com.example.shop.auth.controller

import com.example.shop.auth.domain.Email
import com.example.shop.auth.models.EmailPasswordLoginRequest
import com.example.shop.auth.models.ThirdPartyOauthTokenLoginRequest
import com.example.shop.auth.models.TokenResponse
import com.example.shop.auth.security.third_party.enums.ThirdPartyAuthenticationVendor
import com.example.shop.auth.services.facades.LoginService
import com.example.shop.common.response.GlobalResponse
import com.example.shop.constants.LOGIN_URI
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(LOGIN_URI)
class LoginController(
    private val loginService: LoginService
) {
    @PostMapping("/email")
    fun loginWithEmailAndPassword(
        response: HttpServletResponse,
        @RequestBody reqBody: EmailPasswordLoginRequest,
    ): GlobalResponse<TokenResponse> {
        return loginService.emailLogin(Email(reqBody.email), reqBody.password, response).let {
            GlobalResponse.create(it)
        }
    }

    @PostMapping("/oauth")
    fun loginWithOauthVendor(
        response: HttpServletResponse,
        @RequestParam(required = true) vendorName: String,
        @RequestBody reqBody: ThirdPartyOauthTokenLoginRequest,
    ): GlobalResponse<TokenResponse> {
        val vendor = ThirdPartyAuthenticationVendor.fromString(vendorName)
        return loginService.oauthLogin(vendor, reqBody.token, response).let {
            GlobalResponse.create(it)
        }
    }
}
