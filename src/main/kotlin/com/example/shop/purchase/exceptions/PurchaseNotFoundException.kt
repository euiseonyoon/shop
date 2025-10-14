package com.example.shop.purchase.exceptions

import com.example.shop.common.exceptions.CustomUncheckedException
import org.springframework.http.HttpStatus

class PurchaseNotFoundException(
    orderId: String
) : CustomUncheckedException("구매를 찾을 수 없습니다. orderId: $orderId", HttpStatus.NOT_FOUND)

