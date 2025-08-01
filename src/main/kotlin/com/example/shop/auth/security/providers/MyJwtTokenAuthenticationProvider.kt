package com.example.shop.auth.security.providers

import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.models.UserIdEmailAuthenticationToken
import com.example.shop.auth.services.AccountService
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken

class MyJwtTokenAuthenticationProvider(
    private val jwtTokenHelper: MyJwtTokenHelper,
    private val accountService: AccountService,
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val authenticationToken = authentication as? BearerTokenAuthenticationToken
            ?: return authentication

        try {
            val accessTokenClaims = jwtTokenHelper.parseAccessToken(authenticationToken.token)
            val authorities = jwtTokenHelper.getAuthorityStringList(accessTokenClaims).map {
                SimpleGrantedAuthority(it)
            }
            val accountEmail = jwtTokenHelper.getAccountEmail(accessTokenClaims)
            val account = accountService.findByEmail(accountEmail) ?:
                throw AuthenticationServiceException("Failed to find the account from database.")

            return UserIdEmailAuthenticationToken(
                account = account,
                authorities = authorities,
            ).apply { isAuthenticated = true }
        } catch (e: JwtException) {
            throw BadCredentialsException("Invalid Access Token: ${e.message}", e)
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: Exception) {
            throw AuthenticationServiceException("An error occurred during access token authentication", e)
        }
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return BearerTokenAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
