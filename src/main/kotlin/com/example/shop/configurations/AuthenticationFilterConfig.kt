package com.example.shop.configurations

import com.example.shop.auth.OAUTH_AUTH_URI_PATTERN
import com.example.shop.auth.security.filters.EmailPasswordAuthenticationFilter
import com.example.shop.auth.security.filters.MyJwtAuthenticationFilter
import com.example.shop.auth.security.filters.ThirdPartyOauthAuthenticationFilter
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
class AuthenticationFilterConfig {
    @Bean
    fun emailPasswordAuthenticationFilter(
        authenticationManager: AuthenticationManager,
        json: Json,
        @Qualifier("myLogInAuthenticationSuccessHandler")
        successLoginHandler: AuthenticationSuccessHandler,
        @Qualifier("myLogInAuthenticationFailureHandler")
        failLoginHandler: AuthenticationFailureHandler,
    ): UsernamePasswordAuthenticationFilter {
        return EmailPasswordAuthenticationFilter(authenticationManager, json).apply {
            setAuthenticationSuccessHandler(successLoginHandler)
            setAuthenticationFailureHandler(failLoginHandler)
        }
    }

    @Bean
    fun thirdPartyOauthAuthenticationFilter(
        authenticationManager: AuthenticationManager,
        json: Json,
        @Qualifier("myLogInAuthenticationSuccessHandler")
        successLoginHandler: AuthenticationSuccessHandler,
        @Qualifier("myLogInAuthenticationFailureHandler")
        failLoginHandler: AuthenticationFailureHandler,
    ): AbstractAuthenticationProcessingFilter {
        return ThirdPartyOauthAuthenticationFilter(
            OAUTH_AUTH_URI_PATTERN,
            authenticationManager,
            json,
        ).apply {
            setAuthenticationSuccessHandler(successLoginHandler)
            setAuthenticationFailureHandler(failLoginHandler)
        }
    }

    @Bean
    fun myJwtAuthenticationFilter(
        authenticationManager: AuthenticationManager,
        @Qualifier("customJwtAuthenticationConverter")
        authenticationConverter: AuthenticationConverter,
        @Qualifier("myJwtAuthenticationSuccessHandler")
        noOpAuthenticationSuccessHandler: AuthenticationSuccessHandler
    ): OncePerRequestFilter {
        return MyJwtAuthenticationFilter(
            authenticationManager,
            authenticationConverter,
            noOpAuthenticationSuccessHandler,
        )
    }
}
