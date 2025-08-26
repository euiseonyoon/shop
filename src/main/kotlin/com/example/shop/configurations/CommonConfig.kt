package com.example.shop.configurations

import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommonConfig {
    @Bean
    fun json(): Json = Json { ignoreUnknownKeys = true }
}
