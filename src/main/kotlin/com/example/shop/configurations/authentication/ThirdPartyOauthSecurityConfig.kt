package com.example.shop.configurations.authentication

import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import com.example.shop.auth.security.user_services.GoogleOidcUserService
import com.example.shop.auth.security.user_services.OauthAuthenticatedUserAutoRegisterer
import com.example.shop.auth.security.user_services.ThirdPartyUserServiceManager
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class ThirdPartyOauthSecurityConfig {
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
}

