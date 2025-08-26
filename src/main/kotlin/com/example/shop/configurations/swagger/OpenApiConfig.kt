package com.example.shop.configurations.swagger

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(Info().title("Shop API").version("v1.0"))
            .addSecurityItem(SecurityRequirement().addList(OpenApiConfigHelper.securitySchemeName))
            .components(OpenApiConfigHelper.makeComponents())
            // 수동으로 Login 관련 path들을 추가한다.
            .paths(OpenApiConfigHelper.makeLogInPaths())
    }
}
