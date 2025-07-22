package com.example.shop.security.jwt_helper

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class MyJwtTokenHelperImpl : MyJwtTokenHelper {

    val ACCESS_EXPIRATION_MILLISECONDS: Long = 1000 * 60 * 30 // 1시간
    val REFRESH_EXPIRATION_MILLISECONDS: Long = 1000 * 60 * 60 * 24 * 7 // 7일

    @Value("\${jwt.access_secret}")
    lateinit var encodedAccessSecretKey: String

    @Value("\${jwt.refresh_secret}")
    lateinit var encodedRefreshSecretKey: String

    @Value("\${jwt.issuer}")
    lateinit var issuer: String

    private val accessSecretKey by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedAccessSecretKey)) }
    private val refreshSecretKey by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedRefreshSecretKey)) }

    override fun createAccessToken(email: String): String {
        return createToken(email, ACCESS_EXPIRATION_MILLISECONDS, accessSecretKey)
    }

    override fun createRefreshToken(email: String): String {
        return createToken(email, REFRESH_EXPIRATION_MILLISECONDS, refreshSecretKey)
    }

    private fun createToken(
        email: String,
        durationMs: Long,
        secretKey: SecretKey,
    ): String {
        // TODO: JWT 토큰 생성시 추가해야 할 것들이 있는지 권장사항을 조사해보고 처리하자.
        return Jwts
            .builder()
            .setSubject(email)
            .setIssuedAt(Date())
            .setIssuer(issuer)
            .setExpiration(Date(System.currentTimeMillis() + durationMs))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }
}

