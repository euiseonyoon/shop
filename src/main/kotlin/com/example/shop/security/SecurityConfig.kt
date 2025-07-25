package com.example.shop.security

import com.example.shop.security.filters.EmailPasswordAuthenticationFilter
import com.example.shop.security.filters.ThirdPartyOidcTokenAuthenticationFilter
import com.example.shop.security.handlers.MyLogInAuthenticationFailureHandler
import com.example.shop.security.handlers.MyLogInAuthenticationSuccessHandler
import com.example.shop.security.jwt_helper.GoogleJwtDecoder
import com.example.shop.security.jwt_helper.MyJwtTokenHelper
import com.example.shop.security.third_party_auth.interfaces.ThirdPartyAuthenticationUserService
import com.example.shop.security.third_party_auth.user_services.GoogleOidcUserService
import com.example.shop.security.third_party_auth.user_services.ThirdPartyUserServiceManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

    companion object {
        val EMAIL_PASSWORD_AUTH_URI = "/login/form"
        val OAUTH_AUTH_URI_PATTERN = "/login/oauth/*"
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun googleUserService(): ThirdPartyAuthenticationUserService {
        return GoogleOidcUserService(GoogleJwtDecoder())
    }

    @Bean
    fun thirdPartyAuthUserServiceManager(
        services: List<ThirdPartyAuthenticationUserService>,
    ): ThirdPartyUserServiceManager = ThirdPartyUserServiceManager(services)

    @Bean
    fun authenticationManager(
        passwordEncoder: PasswordEncoder,
        dataSource: DataSource,
        thirdPartyAuthUserServiceManager: ThirdPartyUserServiceManager
    ): AuthenticationManager {
        // /login/form : email+password로 로그인하는 유저들
        // https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jdbc.html#servlet-authentication-jdbc-schema
        val emailPasswordAuthenticationProvider = DaoAuthenticationProvider(JdbcUserDetailsManager(dataSource))
        emailPasswordAuthenticationProvider.setPasswordEncoder(passwordEncoder)

        val thirdPartyOidcAuthenticationProvider = ThirdPartyOauthAuthenticationProvider(thirdPartyAuthUserServiceManager)

        // 나의 모든 api 호출시, access token이 없으면 안된다.
        // val myJwtAuthenticationProvider = JwtAuthenticationProvider()

        return ProviderManager(emailPasswordAuthenticationProvider, thirdPartyOidcAuthenticationProvider)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config =
            CorsConfiguration().apply {
                allowedOrigins = listOf("*")
                allowedMethods = listOf("*") // 임시로 모든 메소드 허용
                allowedHeaders = listOf("*")
                allowCredentials = true
                maxAge = 3600L
            }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }

    private fun makeBaseHttpSecurity(http: HttpSecurity): HttpSecurity {
        http.csrf { it.disable() }
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
        return http
    }

    @Bean
    @Order(1)
    fun emailPasswordAuthenticateFilterChain(
        http: HttpSecurity,
        authenticationManager: AuthenticationManager,
        myJwtTokenHelper: MyJwtTokenHelper,
    ): SecurityFilterChain {
        val emailPasswordAuthenticationFilter = EmailPasswordAuthenticationFilter(authenticationManager).apply {
            setAuthenticationSuccessHandler(MyLogInAuthenticationSuccessHandler(myJwtTokenHelper))
            setAuthenticationFailureHandler(MyLogInAuthenticationFailureHandler())
        }
        return makeBaseHttpSecurity(http)
            .securityMatcher(EMAIL_PASSWORD_AUTH_URI)
            .addFilterAt(emailPasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    @Order(2)
    fun thirdPartyOauthAuthenticateFilterChain(
        http: HttpSecurity,
        authenticationManager: AuthenticationManager,
        myJwtTokenHelper: MyJwtTokenHelper,
    ): SecurityFilterChain {
        val thirdPartyOauthAuthenticationFilter =
            ThirdPartyOidcTokenAuthenticationFilter(OAUTH_AUTH_URI_PATTERN, authenticationManager)
                .apply {
                    setAuthenticationSuccessHandler(MyLogInAuthenticationSuccessHandler(myJwtTokenHelper))
                    setAuthenticationFailureHandler(MyLogInAuthenticationFailureHandler())
                }
        return makeBaseHttpSecurity(http)
            .securityMatcher(OAUTH_AUTH_URI_PATTERN)
            .addFilterAt(thirdPartyOauthAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
