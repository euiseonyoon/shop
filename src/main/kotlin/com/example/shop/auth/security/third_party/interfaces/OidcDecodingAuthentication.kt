package com.example.shop.auth.security.third_party.interfaces

import org.springframework.security.oauth2.jwt.JwtDecoder

interface OidcDecodingAuthentication : ThirdPartyAuthenticationUserService {
    val jwtDecoder: JwtDecoder

    val nameAttributeKey: String
}
