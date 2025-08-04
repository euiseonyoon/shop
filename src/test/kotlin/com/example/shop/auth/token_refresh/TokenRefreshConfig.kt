package com.example.shop.auth.token_refresh

import com.example.shop.auth.TestConstants.Companion.TEST_PSWD
import com.example.shop.auth.login_test.email_password.TestPasswordGenerator
import com.example.shop.auth.security.utils.PasswordGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class TokenRefreshConfig {

    @Bean
    @Primary
    fun passwordGenerator(): PasswordGenerator {
        return TestPasswordGenerator(fixedPassword = TEST_PSWD)
    }
}
