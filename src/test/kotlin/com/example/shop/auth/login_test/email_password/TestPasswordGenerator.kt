package com.example.shop.auth.login_test.email_password

import com.example.shop.auth.security.utils.PasswordGenerator

class TestPasswordGenerator(
    private val fixedPassword: String
) : PasswordGenerator {
    override fun generatePassword(length: Int?): String {
        return fixedPassword
    }
}
