package com.example.shop.auth.security.third_party.utils

interface PasswordGenerator {
    fun generatePassword(length: Int? = null): String
}
