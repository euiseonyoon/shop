package com.example.shop.products.exceptions

import com.example.shop.common.exceptions.CustomUncheckedException
import org.springframework.http.HttpStatus

class ProductUnavailableException(
    productId: Long,
    reason: String,
) : CustomUncheckedException("상품ID: ${productId}, 원인: $reason", HttpStatus.CONFLICT)
