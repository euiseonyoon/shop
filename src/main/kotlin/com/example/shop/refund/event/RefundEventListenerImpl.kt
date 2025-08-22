package com.example.shop.refund.event

import com.example.shop.refund.event.models.RefundEventForAdmin
import com.example.shop.refund.event.models.RefundEventForUser
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class RefundEventListenerImpl : RefundEventListener {
    @EventListener
    override fun handleEventForAdmin(event: RefundEventForAdmin) {
        // TODO: 어드민에게 refund 요청이 들어옴을 알려준다.
    }

    @EventListener
    override fun handleEventForUser(event: RefundEventForUser) {
        // TODO: 유저에게 refund에 대한 결과를 알려준다.
    }
}
