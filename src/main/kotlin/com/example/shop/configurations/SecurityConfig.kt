package com.example.shop.configurations

import com.example.shop.auth.ADMIN_NAME
import com.example.shop.auth.EMAIL_PASSWORD_AUTH_URI
import com.example.shop.auth.OAUTH_AUTH_URI_PATTERN
import com.example.shop.auth.PERMIT_ALL_END_POINTS
import com.example.shop.auth.security.filters.EmailPasswordAuthenticationFilter
import com.example.shop.auth.security.filters.ThirdPartyOauthAuthenticationFilter
import com.example.shop.auth.security.handlers.MyLogInAuthenticationFailureHandler
import com.example.shop.auth.security.handlers.MyLogInAuthenticationSuccessHandler
import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.security.filters.MyJwtAuthenticationFilter
import com.example.shop.auth.security.filters.authentication_converter.CustomJwtAuthenticationConverter
import com.example.shop.auth.security.handlers.MyJwtAuthenticationSuccessHandler
import com.example.shop.auth.security.providers.MyJwtTokenAuthenticationProvider
import com.example.shop.auth.security.providers.ThirdPartyOauthAuthenticationProvider
import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import com.example.shop.auth.security.user_services.EmailPasswordUserDetailService
import com.example.shop.auth.security.user_services.GoogleOidcUserService
import com.example.shop.auth.security.user_services.OauthAuthenticatedUserAutoRegisterer
import com.example.shop.auth.security.user_services.ThirdPartyUserServiceManager
import com.example.shop.auth.security.utils.MyJwtTokenExtractor
import com.example.shop.auth.services.AccountService
import com.example.shop.common.utils.CustomAuthorityUtils
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.AuthorizationFilter
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

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

    @Bean
    fun authenticationManager(
        passwordEncoder: PasswordEncoder,
        thirdPartyAuthUserServiceManager: ThirdPartyUserServiceManager,
        accountService: AccountService,
        jwtTokenHelper: MyJwtTokenHelper,
        customAuthorityUtils: CustomAuthorityUtils,
    ): AuthenticationManager {
        // /login/form : email+password로 로그인하는 유저들
        // https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jdbc.html#servlet-authentication-jdbc-schema
        val emailPasswordAuthenticationProvider = DaoAuthenticationProvider(
            EmailPasswordUserDetailService(accountService, customAuthorityUtils)
        )
        emailPasswordAuthenticationProvider.setPasswordEncoder(passwordEncoder)

        val thirdPartyOidcAuthenticationProvider = ThirdPartyOauthAuthenticationProvider(thirdPartyAuthUserServiceManager)

        val jwtTokenAuthenticationProvider = MyJwtTokenAuthenticationProvider(
            jwtTokenHelper,
            accountService,
        )

        return ProviderManager(
            emailPasswordAuthenticationProvider,
            thirdPartyOidcAuthenticationProvider,
            jwtTokenAuthenticationProvider,
        )
    }

    @Bean
    fun myAuthenticationConverter(
        myJwtTokenExtractor: MyJwtTokenExtractor
    ): AuthenticationConverter = CustomJwtAuthenticationConverter(myJwtTokenExtractor)

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
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
        return http
    }

    @Bean
    @Order(1)
    fun loginAuthenticationFilterChain(
        http: HttpSecurity,
        authenticationManager: AuthenticationManager,
        myJwtTokenHelper: MyJwtTokenHelper,
        json: Json,
    ): SecurityFilterChain {
        val successLoginHandler = MyLogInAuthenticationSuccessHandler(myJwtTokenHelper, json)
        val failLoginHandler = MyLogInAuthenticationFailureHandler()

        val emailPasswordAuthenticationFilter = EmailPasswordAuthenticationFilter(authenticationManager, json).apply {
            setAuthenticationSuccessHandler(successLoginHandler)
            setAuthenticationFailureHandler(failLoginHandler)
        }

        val thirdPartyOauthAuthenticationFilter = ThirdPartyOauthAuthenticationFilter(
            OAUTH_AUTH_URI_PATTERN,
            authenticationManager,
            json,
        ).apply {
            setAuthenticationSuccessHandler(successLoginHandler)
            setAuthenticationFailureHandler(failLoginHandler)
        }

        return makeBaseHttpSecurity(http)
            .securityMatcher(OAUTH_AUTH_URI_PATTERN, EMAIL_PASSWORD_AUTH_URI)
            .addFilterBefore(thirdPartyOauthAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAt(emailPasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    /**
     * NOTE:
     *      아래 코드처럼 하는 것 보다
     *      allowAllFilterChain을 따로 만드는 것이 효율적이다.
     *      아래 코드처럼 하면, 아래의 과정을 거친다.
     *
     *      1. jwtFilter의 authentication 과정을 거친다 (jwt 토큰 decoding 등등의 과정이 발생)
     *      2. 유저정보(Authentication)이 null 이다
     *      3. AuthorizationFilter(인가 필터)에 null인 Authentication이 전달된다.
     *      4. 하지만 permitAll에 해당되는 request 라면 Authentication 결과에 상관없이 진행한다.
     *
     *      그래서 쓸데 없는 jwt 토큰 검증 과정을 거치게 된다.
     *
     *     http
     *     .authorizeHttpRequests { auth ->
     *         auth
     *             .requestMatchers(*PERMIT_ALL_END_POINTS.toTypedArray()).permitAll()
     *             .requestMatchers("/admin/`**").hasRole(ADMIN_NAME)
     *             .anyRequest().authenticated()
     *     }
     *     .addFilterBefore(jwtTokenFilter, AuthorizationFilter::class.java)
     *
     * **/
    @Bean
    @Order(2)
    fun permitAllFilterChain(
        http: HttpSecurity,
        authenticationManager: AuthenticationManager,
        myJwtTokenHelper: MyJwtTokenHelper,
    ): SecurityFilterChain {
        return makeBaseHttpSecurity(http)
            .securityMatcher(*PERMIT_ALL_END_POINTS.toTypedArray())
            .build()
    }

    @Bean
    @Order(3)
    fun jwtAuthenticationFilterChain(
        http: HttpSecurity,
        authenticationManager: AuthenticationManager,
        myJwtTokenHelper: MyJwtTokenHelper,
        authenticationConverter: AuthenticationConverter
    ): SecurityFilterChain {
        val jwtTokenFilter = MyJwtAuthenticationFilter(authenticationManager, authenticationConverter).apply {
            successHandler = MyJwtAuthenticationSuccessHandler()
        }
        /**
         * 아래처럼  securityContext{}를 통해 SecurityContextRepository를 지정하지 않으면
         * `DelegatingSecurityContextRepository`가 default로 사용된다.
         *      DelegatingSecurityContextRepository는
         *      1. 요청에 세션 ID(JSESSIONID)가 있으면 -> HttpSessionSecurityContextRepository를 사용하여 HttpSession에 인증/인가 정보를 저장한다.
         *      2. 1번의 경우가 아니면 -> 내부적으로 설정된 다른 SecurityContextRepository에 위임합니다.
         *
         * 우리의 경우처럼, stateless한 API를 사용하는 경우,
         * `RequestAttributeSecurityContextRepository`를 사용해야 한다.
         *
         * RequestAttributeSecurityContextRepository()를 Bean으로 정의 한뒤,
         * 주입받고, 아래 코드 처럼 할 수도 있다.
         *
         *             .securityContext { securityContext ->
         *                 securityContext.securityContextRepository(securityContextRepository)
         *             }
         *
         * 하지만 아래 코드 처럼한다면, DelegatingSecurityContextRepository가 알아서, RequestAttributeSecurityContextRepository를
         * 사용한다.
         *              .sessionManagement { session ->
         *             session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
         *         }
         *
         * */
        return makeBaseHttpSecurity(http)
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/admin/**").hasRole(ADMIN_NAME)
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtTokenFilter, AuthorizationFilter::class.java)
            .build()
    }
}
