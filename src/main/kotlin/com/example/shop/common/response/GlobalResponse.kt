package com.example.shop.common.response

import com.example.shop.common.exceptions.CustomExceptionInterface
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.Serializable
import org.springframework.http.HttpStatus

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

        fun <T> createErrorRes(response: HttpServletResponse, errorMsg: String, status: HttpStatus?): GlobalResponse<T> {
            if (status != null) {
                response.status = status.value()
            }
            return create(true, null, errorMsg)
        }

        fun <T> createErrorRes(
            response: HttpServletResponse,
            exception: Exception,
            status: HttpStatus?,
        ): GlobalResponse<T> {
            val (desiredStatus, errorMsg) = if (exception is CustomExceptionInterface) {
                exception.status to exception.message
            } else {
                status to (exception.message ?: "no error message.")
            }
            desiredStatus?.let { response.status = it.value() }
            return createErrorRes(response, errorMsg, desiredStatus)
        }
    }
}
