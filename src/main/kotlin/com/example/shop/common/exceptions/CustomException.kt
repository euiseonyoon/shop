package com.example.shop.common.exceptions

import org.springframework.http.HttpStatus

abstract class CustomException(
    override val message: String,
    override val status: HttpStatus? = null
) : Exception(message), CustomExceptionInterface
