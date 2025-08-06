package com.example.shop.configurations.authentication

import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import com.example.shop.auth.security.user_services.GoogleOidcUserService
import com.example.shop.auth.security.user_services.OauthAuthenticatedUserAutoRegisterer
import com.example.shop.auth.security.user_services.ThirdPartyUserServiceManager
import com.example.shop.common.utils.CustomAuthorityUtils
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class ThirdPartyOauthSecurityConfig {
    @Bean
    fun googleUserService(
        oauth2AuthenticatedAutoRegisterer: OauthAuthenticatedUserAutoRegisterer,
        customAuthorityUtils: CustomAuthorityUtils
    ): ThirdPartyAuthenticationUserService {
        return GoogleOidcUserService(oauth2AuthenticatedAutoRegisterer, customAuthorityUtils)
    }

    @Bean
    fun thirdPartyAuthUserServiceManager(
        services: List<ThirdPartyAuthenticationUserService>,
    ): ThirdPartyUserServiceManager = ThirdPartyUserServiceManager(services)
}

