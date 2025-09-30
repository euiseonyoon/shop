package com.example.shop.common

import kotlinx.serialization.json.Json
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc

@TestConfiguration
class EasyAccessTokenTestConfig {
    @Bean
    fun accessTokenGetter(
        mockMvc: MockMvc,
        json: Json,
    ): AccessTokenGetter {
        return AccessTokenGetter(mockMvc, json)
    }
}
