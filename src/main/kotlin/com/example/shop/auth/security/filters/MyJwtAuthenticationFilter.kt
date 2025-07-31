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

    private val securityContextRepository: SecurityContextRepository =
        RequestAttributeSecurityContextRepository()

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
        val context = securityContextHolderStrategy.createEmptyContext()
        context.authentication = authentication
        securityContextHolderStrategy.context = context
        securityContextRepository.saveContext(context, request, response)
        successHandler.onAuthenticationSuccess(request, response, chain, authentication)
    }
}
