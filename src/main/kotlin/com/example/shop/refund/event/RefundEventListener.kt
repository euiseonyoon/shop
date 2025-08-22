package com.example.shop.refund.event

import com.example.shop.refund.event.models.RefundEventForAdmin
import com.example.shop.refund.event.models.RefundEventForUser

interface RefundEventListener {
    fun handleEventForAdmin(event: RefundEventForAdmin)

    fun handleEventForUser(event: RefundEventForUser)
}
