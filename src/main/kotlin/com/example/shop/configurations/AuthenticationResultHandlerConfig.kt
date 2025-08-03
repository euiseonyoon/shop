package com.example.shop.configurations

import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.security.handlers.MyJwtAuthenticationSuccessHandler
import com.example.shop.auth.security.handlers.MyLogInAuthenticationFailureHandler
import com.example.shop.auth.security.handlers.MyLogInAuthenticationSuccessHandler
import com.example.shop.auth.utils.RefreshTokenStateHelper
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

@Configuration
class AuthenticationResultHandlerConfig {
    @Bean
    fun myLogInAuthenticationSuccessHandler(
        jwtHelper: MyJwtTokenHelper,
        json: Json,
        refreshTokenStateHelper: RefreshTokenStateHelper
    ): AuthenticationSuccessHandler {
        return MyLogInAuthenticationSuccessHandler(jwtHelper, json, refreshTokenStateHelper)
    }

    @Bean
    fun myLogInAuthenticationFailureHandler(json: Json): AuthenticationFailureHandler {
        return MyLogInAuthenticationFailureHandler(json)
    }

    @Bean
    fun myJwtAuthenticationSuccessHandler(): AuthenticationSuccessHandler {
        return MyJwtAuthenticationSuccessHandler()
    }
}
