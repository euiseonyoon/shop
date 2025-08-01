package com.example.shop.common.apis

import kotlinx.serialization.Serializable

@Serializable
data class GlobalResponse<T>(
    val isError: Boolean,
    val result: T?,
    val errorMsg: String?
) {
    init {
        require((result == null) xor (errorMsg == null)) {
            "Either result or errorMsg must be not null."
        }
    }

    companion object {
        fun <T> create(isError: Boolean, result: T?, errorMsg: String?) = GlobalResponse(isError, result, errorMsg)

        fun <T> create(result: T): GlobalResponse<T> {
            return create(false, result, null)
        }

        fun <T> createErrorRes(errorMsg: String): GlobalResponse<T> {
            return create(true, null, errorMsg)
        }
    }
}
