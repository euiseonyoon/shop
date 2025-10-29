package com.example.shop.auth.security.domain

import kotlinx.serialization.Serializable

@Serializable
data class HashedRefreshToken(
    val value: String
) {
    override fun equals(other: Any?): Boolean {
        if (other !is HashedRefreshToken) return false
        return other.value == this.value
    }

    override fun hashCode(): Int = this.value.hashCode()
}
