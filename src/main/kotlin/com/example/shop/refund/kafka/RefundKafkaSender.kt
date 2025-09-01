package com.example.shop.refund.kafka

import com.example.shop.common.apis.models.AccountDto
import com.example.shop.kafka.KafkaMessageSender
import com.example.shop.refund.domain.Refund
import com.example.shop.refund.models.RefundKafkaDto
import org.springframework.stereotype.Component

@Component
class RefundKafkaSender(
    private val kafkaMessageSender: KafkaMessageSender
) {
    private fun refundToKafkaDto(refund: Refund): RefundKafkaDto {
        val account = refund.purchase!!.account!!
        return RefundKafkaDto(
            refundId = refund.id!!,
            refundStatus = refund.status,
            accountInfo = AccountDto(
                id = account.id!!,
                email = account.email!!,
                enabled = account.enabled,
                nickname = account.nickname
            )
        )
    }

    fun notifyAdminRefundRequested(refund: Refund) {
        kafkaMessageSender.sendRefundMessageToAdmin(refundToKafkaDto(refund))
    }

    fun notifyUserRefundResult(refund: Refund) {
        kafkaMessageSender.sendRefundMessageToUser(refundToKafkaDto(refund))
    }
}
