package com.example.shop.common

import com.example.shop.auth.services.AccountService
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.test.web.servlet.MockMvc

@Configuration
@Profile("test")
class EasyAccessTokenTestConfig {
    @Autowired
    lateinit var accountService: AccountService

    @Bean
    fun accessTokenGetter(
        accountService: AccountService,
        mockMvc: MockMvc,
        json: Json,
    ): AccessTokenGetter {
        return AccessTokenGetter(accountService, mockMvc, json)
    }
}
