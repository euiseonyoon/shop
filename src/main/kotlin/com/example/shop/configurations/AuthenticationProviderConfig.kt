package com.example.shop.configurations

import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.security.providers.MyJwtTokenAuthenticationProvider
import com.example.shop.auth.security.providers.ThirdPartyOauthAuthenticationProvider
import com.example.shop.auth.security.user_services.EmailPasswordUserDetailService
import com.example.shop.auth.security.user_services.ThirdPartyUserServiceManager
import com.example.shop.auth.services.AccountService
import com.example.shop.common.utils.CustomAuthorityUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class AuthenticationProviderConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun emailPasswordUserDetailService(
        accountService: AccountService,
        customAuthorityUtils: CustomAuthorityUtils
    ): UserDetailsService {
        return EmailPasswordUserDetailService(accountService, customAuthorityUtils)
    }

    @Bean
    fun emailPasswordAuthenticationProvider(
        passwordEncoder: PasswordEncoder,
        @Qualifier("emailPasswordUserDetailService")
        emailPasswordUserDetailService: UserDetailsService,
    ): DaoAuthenticationProvider {
        // email + password로 로그인
        return DaoAuthenticationProvider(emailPasswordUserDetailService).apply {
            setPasswordEncoder(passwordEncoder)
        }
    }

    @Bean
    fun thirdPartyOauthAuthenticationProvider(
        @Qualifier("thirdPartyAuthUserServiceManager")
        thirdPartyAuthUserServiceManager: ThirdPartyUserServiceManager,
    ): AuthenticationProvider {
        return ThirdPartyOauthAuthenticationProvider(thirdPartyAuthUserServiceManager)
    }

    @Bean
    fun myJwtTokenAuthenticationProvider(
        jwtTokenHelper: MyJwtTokenHelper,
        accountService: AccountService,
    ): AuthenticationProvider {
        return MyJwtTokenAuthenticationProvider(jwtTokenHelper, accountService)
    }

    @Bean
    fun authenticationManager(
        @Qualifier("emailPasswordAuthenticationProvider")
        emailPasswordAuthenticationProvider: DaoAuthenticationProvider,
        @Qualifier("thirdPartyOauthAuthenticationProvider")
        thirdPartyOidcAuthenticationProvider: AuthenticationProvider,
        @Qualifier("myJwtTokenAuthenticationProvider")
        jwtTokenAuthenticationProvider: AuthenticationProvider,
    ): AuthenticationManager {
        return ProviderManager(
            emailPasswordAuthenticationProvider,
            thirdPartyOidcAuthenticationProvider,
            jwtTokenAuthenticationProvider,
        )
    }
}
