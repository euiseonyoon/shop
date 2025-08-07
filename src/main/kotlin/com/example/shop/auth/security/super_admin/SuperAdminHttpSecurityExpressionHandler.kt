package com.example.shop.auth.security.super_admin

import com.example.shop.constants.ROLE_SUPER_ADMIN
import com.example.shop.auth.models.AccountAuthenticationToken
import org.springframework.expression.EvaluationContext
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import java.util.function.Supplier

class SuperAdminHttpSecurityExpressionHandler(
    private val superAdminEmail: String
): DefaultHttpSecurityExpressionHandler() {
    override fun createEvaluationContext(
        authentication: Supplier<Authentication>,
        context: RequestAuthorizationContext,
    ): EvaluationContext {
        val possibleSuperAdminAuthentication = updateAuthoritiesIfSuperAdmin(authentication.get())

        return super.createEvaluationContext(Supplier { possibleSuperAdminAuthentication }, context)
    }

    private fun updateAuthoritiesIfSuperAdmin(authentication: Authentication): Authentication {
        if (authentication !is AccountAuthenticationToken) return authentication

        if (authentication.email != superAdminEmail) return authentication

        val authorities = authentication.getAuthorities().toMutableSet()
        authorities.add(SimpleGrantedAuthority(ROLE_SUPER_ADMIN))

        return AccountAuthenticationToken(
            accountId = authentication.accountId,
            authorities = authorities.toList(),
            email = authentication.email
        ).apply { isAuthenticated = authentication.isAuthenticated }
    }
}
