package com.example.shop.auth

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("test")
@Configuration
class TestConstants {
    companion object {
        const val TEST_EMAIL = "test123@gmail.com"
        const val TEST_PSWD = "test123word"
    }
}
