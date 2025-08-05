package com.example.shop.auth.jwt_helpers

import com.example.shop.auth.ACCESS_TOKEN_EXPIRATION_MS
import com.example.shop.auth.REFRESH_TOKEN_EXPIRATION_MS
import com.example.shop.auth.REFRESH_TOKEN_KEY
import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper.Companion.AUTH_CLAIM_KEY
import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper.Companion.AUTH_STRING_DELIMITER
import com.example.shop.auth.jwt_helpers.MyJwtTokenHelper.Companion.EMAIL_CLAIM_KEY
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class MyJwtTokenHelperImpl : MyJwtTokenHelper {
    override val accessTokenExpirationMs: Long = ACCESS_TOKEN_EXPIRATION_MS
    override val refreshTokenExpirationMs: Long = REFRESH_TOKEN_EXPIRATION_MS

    @Value("\${jwt.access_secret}")
    lateinit var encodedAccessSecretKey: String

    @Value("\${jwt.refresh_secret}")
    lateinit var encodedRefreshSecretKey: String

    @Value("\${jwt.issuer}")
    lateinit var issuer: String

    private val accessSecretKey by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedAccessSecretKey)) }
    private val refreshSecretKey by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedRefreshSecretKey)) }


    override fun createAccessToken(accountId: Long, authorities: List<GrantedAuthority>, email: String): String {
        val authoritiesString = authorities.map { it.authority }.joinToString(AUTH_STRING_DELIMITER)
        return createToken(
            accountId,
            accessTokenExpirationMs,
            accessSecretKey,
            mapOf(
                AUTH_CLAIM_KEY to authoritiesString,
                EMAIL_CLAIM_KEY to email,
            ),
            null
        )
    }

    override fun createRefreshToken(accountId: Long): String {
        return createToken(
            accountId,
            refreshTokenExpirationMs,
            refreshSecretKey,
            null,
            UUID.randomUUID().toString()
        )
    }

    override fun parseAccessToken(accessToken: String): Claims {
        return paresToken(accessToken, accessSecretKey)
    }

    override fun parseRefreshToken(refreshToken: String): Claims {
        return paresToken(refreshToken, refreshSecretKey)
    }

    private fun getFromClaims(key: String, claims: Claims): String {
        return claims[key] as? String ?:
            throw BadCredentialsException("Could not find $key from the claims.")
    }

    override fun getEmail(claims: Claims): String {
        return getFromClaims(EMAIL_CLAIM_KEY, claims)
    }

    override fun getAuthorityStringList(claims: Claims): List<String> {
        val authoritiesString = getFromClaims(AUTH_CLAIM_KEY, claims)
        return authoritiesString.split(AUTH_STRING_DELIMITER)
    }

    override fun getSubject(claims: Claims): Long {
        return claims.subject?.toLong() ?: throw BadJwtException("subject missing from claims.")
    }

    override fun setRefreshTokenOnCookie(response: HttpServletResponse, refreshToken: String) {
        val refreshTokenCookie = Cookie(REFRESH_TOKEN_KEY, refreshToken)
        response.addCookie(
            setSecuredCookie(refreshTokenCookie, (refreshTokenExpirationMs / 1000).toInt())
        )
    }

    override fun deleteRefreshTokenFromCookie(response: HttpServletResponse) {
        val refreshTokenCookie = Cookie(REFRESH_TOKEN_KEY, null)
        response.addCookie(
            setSecuredCookie(refreshTokenCookie, 0)
        )
    }

    private fun setSecuredCookie(cookie: Cookie, maxAgeSec: Int): Cookie {
        cookie.apply {
            isHttpOnly = true // JavaScript 접근 방지
            path = "/"
            maxAge = maxAgeSec
            secure = true
        }
        return cookie
    }

    private fun paresToken(token: String, secretKey: SecretKey): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun createToken(
        accountId: Long,
        durationMs: Long,
        secretKey: SecretKey,
        claims: Map<String, Any>?,
        jti: String?
    ): String {
        val builder = Jwts.builder()

        claims?.let { builder.setClaims(it) }
        jti?.let { builder.setId(it) }

        builder
            .setSubject(accountId.toString())
            .setIssuedAt(Date())
            .setIssuer(issuer)
            .setExpiration(Date(System.currentTimeMillis() + durationMs))
            .signWith(secretKey, SignatureAlgorithm.HS256)

        return builder.compact()
    }
}

