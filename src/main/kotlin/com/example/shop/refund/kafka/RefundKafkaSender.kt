package com.example.shop.refund.kafka

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
    private fun refundToKafkaMessage(accountEmail: String, refund: Refund, type: NotifyType): NotifyKafkaMessage {
        return NotifyKafkaMessage(
            type = type,
            content = NotifyKafkaContent.RefundKafkaDto(
                refundId = refund.id,
                refundStatus = refund.status,
                accountEmail = accountEmail,
            )
        )
    }

    fun notifyAdminRefundRequested(email: String, refund: Refund) {
        kafkaMessageSender.sendNotifyMessage(refundToKafkaMessage(email, refund, NotifyType.NOTIFY_ADMIN_REFUND))
    }

    fun notifyUserRefundResult(email: String, refund: Refund) {
        kafkaMessageSender.sendNotifyMessage(refundToKafkaMessage(email, refund, NotifyType.NOTIFY_USER_REFUND))
    }
}
