package com.example.shop.purchase.exceptions

import com.example.shop.common.exceptions.CustomUncheckedException
import org.springframework.http.HttpStatus

class TossPaymentApiException(
    val errorCode: String,
    val errorMessage: String,
    val httpStatus: HttpStatus
) : CustomUncheckedException("errorCode: $errorCode, errorMessage: $errorMessage", httpStatus)

