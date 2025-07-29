package com.example.shop.common.exceptions

import org.springframework.http.HttpStatus

interface CustomExceptionInterface {
    val message: String
    val status: HttpStatus?
}
