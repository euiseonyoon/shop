package com.example.shop.auth.jwt_helpers

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class MyJwtTokenHelperImpl : MyJwtTokenHelper {
    override val accessTokenExpirationMs: Long = 1000 * 60 * 30 // 1시간
    override val refreshTokenExpirationMs: Long = 1000 * 60 * 60 * 24 * 7 // 7일
    override val authClaimKey: String = "auth"

    @Value("\${jwt.access_secret}")
    lateinit var encodedAccessSecretKey: String

    @Value("\${jwt.refresh_secret}")
    lateinit var encodedRefreshSecretKey: String

    @Value("\${jwt.issuer}")
    lateinit var issuer: String

    private val accessSecretKey by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedAccessSecretKey)) }
    private val refreshSecretKey by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedRefreshSecretKey)) }


    override fun createAccessToken(email: String, authentication: Authentication): String {
        val authoritiesString = authentication.authorities.map { it.authority }.joinToString(",")
        return createToken(
            email,
            accessTokenExpirationMs,
            accessSecretKey,
            mapOf(authClaimKey to authoritiesString),
            null
        )
    }

    override fun createRefreshToken(email: String): String {
        return createToken(
            email,
            refreshTokenExpirationMs,
            refreshSecretKey,
            null,
            UUID.randomUUID().toString()
        )
    }

    private fun createToken(
        email: String,
        durationMs: Long,
        secretKey: SecretKey,
        claims: Map<String, Any>?,
        jti: String?
    ): String {
        val builder = Jwts
            .builder()
            .setSubject(email)
            .setIssuedAt(Date())
            .setIssuer(issuer)
            .setExpiration(Date(System.currentTimeMillis() + durationMs))
            .signWith(secretKey, SignatureAlgorithm.HS256)

        jti?.let { builder.setId(it) }
        claims?.let { builder.setClaims(it) }
        return builder.compact()
    }
}

