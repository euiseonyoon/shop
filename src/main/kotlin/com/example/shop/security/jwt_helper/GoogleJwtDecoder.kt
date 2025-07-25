package com.example.shop.security.jwt_helper

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

class GoogleJwtDecoder : JwtDecoder {
    val jwkSetUri = "https://www.googleapis.com/oauth2/v3/certs"
    val decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()

    override fun decode(token: String): Jwt = decoder.decode(token)
}
