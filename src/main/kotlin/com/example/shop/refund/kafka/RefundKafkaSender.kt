package com.example.shop.refund.kafka

import com.example.shop.common.apis.models.AccountDto
import com.example.shop.kafka.KafkaMessageSender
import com.example.shop.kafka.notify_topic.enums.NotifyType
import com.example.shop.kafka.notify_topic.models.NotifyKafkaContent
import com.example.shop.kafka.notify_topic.models.NotifyKafkaMessage
import com.example.shop.refund.domain.Refund
import org.springframework.stereotype.Component

@Component
class RefundKafkaSender(
    private val kafkaMessageSender: KafkaMessageSender
) {
    private fun refundToKafkaMessage(refund: Refund, type: NotifyType): NotifyKafkaMessage {
        val account = refund.purchase!!.account!!
        return NotifyKafkaMessage(
            type = type,
            content = NotifyKafkaContent.RefundKafkaDto(
                refundId = refund.id!!,
                refundStatus = refund.status,
                accountInfo = AccountDto(
                    id = account.id!!,
                    email = account.email!!,
                    enabled = account.enabled,
                    nickname = account.nickname
                )
            )
        )
    }

    fun notifyAdminRefundRequested(refund: Refund) {
        kafkaMessageSender.sendNotifyMessage(refundToKafkaMessage(refund, NotifyType.NOTIFY_ADMIN_REFUND))
    }

    fun notifyUserRefundResult(refund: Refund) {
        kafkaMessageSender.sendNotifyMessage(refundToKafkaMessage(refund, NotifyType.NOTIFY_USER_REFUND))
    }
}
