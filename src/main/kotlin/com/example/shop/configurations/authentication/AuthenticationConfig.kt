package com.example.shop.configurations.authentication

import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.security.filters.MyJwtAuthenticationFilter
import com.example.shop.auth.security.authentication_converter.CustomJwtAuthenticationConverter
import com.example.shop.auth.security.handlers.MyJwtAuthenticationSuccessHandler
import com.example.shop.auth.security.providers.MyJwtTokenAuthenticationProvider
import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import com.example.shop.auth.security.user_services.GoogleOidcUserService
import com.example.shop.auth.security.user_services.OauthAuthenticatedUserAutoRegisterer
import com.example.shop.auth.security.user_services.ThirdPartyUserServiceManager
import com.example.shop.auth.security.utils.MyJwtTokenExtractor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
class AuthenticationConfig {
    // ================== LOGIN 관련 ========================
    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun googleUserService(
        oauth2AuthenticatedAutoRegisterer: OauthAuthenticatedUserAutoRegisterer,
    ): ThirdPartyAuthenticationUserService {
        return GoogleOidcUserService(oauth2AuthenticatedAutoRegisterer)
    }

    @Bean
    fun thirdPartyAuthUserServiceManager(
        services: List<ThirdPartyAuthenticationUserService>,
    ): ThirdPartyUserServiceManager = ThirdPartyUserServiceManager(services)


    // ================== JWT 관련 ========================
    @Bean
    fun myJwtTokenAuthenticationProvider(
        jwtTokenHelper: MyJwtTokenHelper,
    ): AuthenticationProvider {
        return MyJwtTokenAuthenticationProvider(jwtTokenHelper)
    }

    @Bean
    fun authenticationManager(
        jwtTokenAuthenticationProvider: AuthenticationProvider,
    ): AuthenticationManager {
        return ProviderManager(
            jwtTokenAuthenticationProvider,
        )
    }

    @Bean
    fun myJwtAuthenticationSuccessHandler(): AuthenticationSuccessHandler {
        return MyJwtAuthenticationSuccessHandler()
    }

    @Bean
    fun customJwtAuthenticationConverter(
        myJwtTokenExtractor: MyJwtTokenExtractor
    ): AuthenticationConverter {
        return CustomJwtAuthenticationConverter(myJwtTokenExtractor)
    }

    @Bean
    fun myJwtAuthenticationFilter(
        authenticationManager: AuthenticationManager,
        authenticationConverter: AuthenticationConverter,
    ): OncePerRequestFilter {
        return MyJwtAuthenticationFilter(
            authenticationManager,
            authenticationConverter,
            myJwtAuthenticationSuccessHandler(),
        )
    }

}
