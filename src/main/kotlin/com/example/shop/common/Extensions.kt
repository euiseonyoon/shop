package com.example.shop.common

import com.example.shop.auth.security.domain.HashedRefreshToken
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

fun Int.requireNonNegative(exception: Exception?): Int {
    try {
        require(this >= 0)
    } catch (e: Exception) {
        if (exception != null) {
            throw exception
        } else {
            throw e
        }
    }
    return this
}

fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes: ByteArray = digest.digest(this.toByteArray(StandardCharsets.UTF_8))
    return hashBytes.joinToString("") { String.format("%02x", it) }
}
