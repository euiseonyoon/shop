package com.example.shop.configurations

import com.example.shop.auth.ADMIN_NAME
import com.example.shop.auth.EMAIL_PASSWORD_AUTH_URI
import com.example.shop.auth.OAUTH_AUTH_URI_PATTERN
import com.example.shop.auth.PERMIT_ALL_END_POINTS
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.AuthorizationFilter
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@EnableWebSecurity
class SecurityConfig {
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
        @Qualifier("emailPasswordAuthenticationFilter")
        emailPasswordAuthenticationFilter: UsernamePasswordAuthenticationFilter,
        @Qualifier("thirdPartyOauthAuthenticationFilter")
        thirdPartyOauthAuthenticationFilter: AbstractAuthenticationProcessingFilter
    ): SecurityFilterChain {
        return makeBaseHttpSecurity(http)
            .securityMatcher(OAUTH_AUTH_URI_PATTERN, EMAIL_PASSWORD_AUTH_URI)
            .addFilterBefore(thirdPartyOauthAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAt(emailPasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
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
    fun permitAllFilterChain(http: HttpSecurity): SecurityFilterChain {
        return makeBaseHttpSecurity(http)
            .securityMatcher(*PERMIT_ALL_END_POINTS.toTypedArray())
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            .build()
    }

    @Bean
    @Order(3)
    fun jwtAuthenticationFilterChain(
        http: HttpSecurity,
        @Qualifier("myJwtAuthenticationFilter")
        jwtTokenFilter: OncePerRequestFilter
    ): SecurityFilterChain {
        /**
         * 문제:
         *  MyJwtAuthenticationFilter를 Bean 으로 등록하면 위의 permitAllFilterChain()에 매칭되는 /token/refresh 를 호출해도
         *  MyJwtAuthenticationFilter.doFilterInternal()을 거친다.
         *  추측으로는 MyJwtAuthenticationFilter : AuthenticationFilter : OncePerRequestFilter()라서 ?
         *
         * OncePerRequestFilter는 기본적으로 모든 request에 한번씩 적용된다.
         * 그래서 Bean으로 등록해두면 permitAllFilterChain()의 securityMatcher에 매칭되지 않더라도 적용된것이다.
         * 이걸 원하지 않으면 OncePerRequestFilter는.shouldNotFilter()를 override 한 것을 Bean으로 등록하면 된다.
         *
         * 그러지 않으면 아래처럼 OncePerRequestFilter를 Bean 으로 등록하지 않으면 된다.
         *
         *     val jwtTokenFilter = MyJwtAuthenticationFilter(authenticationManager, authenticationConverter).apply {
         *        successHandler = noOpAuthenticationSuccessHandler
         *     }
         * */

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
