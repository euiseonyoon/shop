package com.example.shop.refund.event

import com.example.shop.common.apis.models.AccountDto
import com.example.shop.kafka.KafkaMessageSender
import com.example.shop.refund.event.models.RefundEventForAdmin
import com.example.shop.refund.event.models.RefundEventForUser
import com.example.shop.refund.models.RefundKafkaDto
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class RefundEventListenerImpl(
    private val kafkaMessageSender: KafkaMessageSender
) : RefundEventListener {
    @EventListener
    override fun handleEventForAdmin(event: RefundEventForAdmin) {
        val refund = event.refund
        val account = event.refund.purchase!!.account!!

        val dto = RefundKafkaDto(
            refundId = refund.id!!,
            refundStatus = refund.status,
            accountInfo = AccountDto(
                id = account.id!!,
                email = account.email!!,
                enabled = account.enabled,
                nickname = account.nickname,
            ),
        )
        kafkaMessageSender.sendRefundMessageToAdmin(dto)
        // TODO: 어드민에게 refund 요청이 들어옴을 알려준다.
    }

    @EventListener
    override fun handleEventForUser(event: RefundEventForUser) {
        // TODO: 유저에게 refund에 대한 결과를 알려준다.
    }
}
