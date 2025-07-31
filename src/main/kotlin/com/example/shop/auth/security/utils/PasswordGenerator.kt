package com.example.shop.auth.security.utils

interface PasswordGenerator {
    fun generatePassword(length: Int? = null): String
}
