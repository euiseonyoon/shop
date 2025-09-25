package com.example.shop.purchase.exceptions

import com.example.shop.common.exceptions.CustomUncheckedException
import org.springframework.http.HttpStatus

class PurchaseByCartException(
    message: String
) : CustomUncheckedException(message, HttpStatus.CONFLICT)
