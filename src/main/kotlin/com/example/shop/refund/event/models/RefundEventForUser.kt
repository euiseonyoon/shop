package com.example.shop.refund.event.models

import com.example.shop.refund.domain.Refund

data class RefundEventForUser(
    val refund: Refund
)
