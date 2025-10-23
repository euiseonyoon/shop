package com.example.shop.common.exceptions

import com.example.shop.common.response.GlobalResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(
        ex: CustomException,
        response: HttpServletResponse
    ): GlobalResponse<Nothing> {
        return GlobalResponse.createErrorRes(response, ex, ex.status)
    }

    @ExceptionHandler(CustomUncheckedException::class)
    fun handleCustomUncheckedException(
        ex: CustomUncheckedException,
        response: HttpServletResponse
    ): GlobalResponse<Nothing> {
        return GlobalResponse.createErrorRes(response, ex, ex.status)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        response: HttpServletResponse
    ): GlobalResponse<Nothing> {
        return GlobalResponse.createErrorRes(
            response = response,
            exception = ex,
            status = HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}
