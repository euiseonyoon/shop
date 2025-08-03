package com.example.shop.auth.security.providers

import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper
import com.example.shop.auth.models.AccountAuthenticationToken
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
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val authenticationToken = authentication as? BearerTokenAuthenticationToken
            ?: return authentication

        try {
            val accessTokenClaims = jwtTokenHelper.parseAccessToken(authenticationToken.token)
            val authorities = jwtTokenHelper.getAuthorityStringList(accessTokenClaims).map {
                SimpleGrantedAuthority(it)
            }
            val accountId = jwtTokenHelper.getSubject(accessTokenClaims)
            /**
             * 아래의 `AccountAuthenticationToken`는 `UsernamePasswordAuthenticationToken` 상속한다.
             * `UsernamePasswordAuthenticationToken` 생성자중, authorities를 사용하는 생성자는
             *  내부에서 이미 authenticated = true로 설정한다.
             * `AccountAuthenticationToken.apply {authenticated = true}`를 실행하면,
             * `UsernamePasswordAuthenticationToken.setAuthenticated()` 내부의 exception이 발생한다.
             *
             * 변경!!:
             * AccountAuthenticationToken : AbstractAuthenticationToken를 상속하게 변경하였다.
             * 따라서 isAuthenticated = true로 설정한다.
             *
             * */
            return AccountAuthenticationToken(
                accountId = accountId,
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
