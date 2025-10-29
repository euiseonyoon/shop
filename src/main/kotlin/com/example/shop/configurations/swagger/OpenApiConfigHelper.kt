package com.example.shop.configurations.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.security.SecurityScheme

class OpenApiConfigHelper {
    companion object {
        val securitySchemeName = "bearerAuth"

        fun makeComponents(): Components {
            return Components().apply {
                // JWT access token을 "Authorization" request 헤더에 넣을 수 있게 한다.
                val jwtScheme = getJwtAuthorizationSchema()
                this.addSecuritySchemes(jwtScheme.first, jwtScheme.second)
            }
        }

        private fun getJwtAuthorizationSchema(): Pair<String, SecurityScheme> {
            return securitySchemeName to SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        }
    }
}
