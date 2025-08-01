package com.example.shop.auth.security.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.authentication.AuthenticationFilter
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository

class MyJwtAuthenticationFilter(
    private val authenticationManager: AuthenticationManager,
    private val authenticationConverter: AuthenticationConverter, // request -> Authentication으로 바꾸는것
) : AuthenticationFilter(authenticationManager, authenticationConverter) {

    /**
     *
     *  1. SecurityContextPersistenceFilter
     *  2. Authentication Filter
     *  3. Authorization Filter
     *  4. 다시 SecurityContextPersistenceFilter
     *
     *  위 과정을 거치는 동안 같은 SecurityContextRepository 가 사용되는것이 좋다. 따라서 아래처럼 되어있던, 기존 코드를 제거한다.
     *
     *  private val securityContextRepository: SecurityContextRepository =
     *         RequestAttributeSecurityContextRepository()
     * */

    /**
     * securityContextHolderStrategy의 실제 객체는 `ThreadLocalSecurityContextHolderStrategy`
     * SecurityContextHolder.initializeStrategy() 참고. (default가 ThreadLocalSecurityContextHolderStrategy)
     * */
    private val securityContextHolderStrategy: SecurityContextHolderStrategy =
        SecurityContextHolder.getContextHolderStrategy()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val authenticationResult = tryAuthentication(request)
            if (authenticationResult == null) {
                filterChain.doFilter(request, response)
                return
            }
            successfulAuthentication(request, response, filterChain, authenticationResult)
        } catch (ex: AuthenticationException) {
            unsuccessfulAuthentication(request, response, ex)
        }
    }

    private fun tryAuthentication(request: HttpServletRequest): Authentication? {
        val authentication = authenticationConverter.convert(request) ?: return null

        return authenticationManager.authenticate(authentication) ?:
        throw ServletException("AuthenticationManager should not return null Authentication object.")
    }

    private fun unsuccessfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, failed: AuthenticationException) {
        securityContextHolderStrategy.clearContext()
        failureHandler.onAuthenticationFailure(request, response, failed)
    }

    private fun successfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain, authentication: Authentication) {
        /**
         * spring Security의 필터 체인에서는
         * SecurityContextHolder가 요청 시작 시점에 이미 존재하거나 (SecurityContextPersistenceFilter가 생성),
         * 최소한 요청 스레드에 연결되어 있다.
         * 그래서 아래처럼 되어 있던, 기존 코드는 잘 못 되었다.
         *
         *      val context = securityContextHolderStrategy.context
         *      context.authentication = authentication
         *
         * 기존에 이미 있는 SecurityContext에 인증이 완료된 유저정보(Authentication)을 적용해야한다.
         * */
        val context = securityContextHolderStrategy.context
        context.authentication = authentication
        successHandler.onAuthenticationSuccess(request, response, chain, authentication)
    }
}
