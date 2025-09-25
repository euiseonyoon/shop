package com.example.shop.products.exceptions

import com.example.shop.common.exceptions.CustomUncheckedException
import org.springframework.http.HttpStatus

class ProductInsufficientStockException(
    msg: String
) : CustomUncheckedException(msg, HttpStatus.CONFLICT)
