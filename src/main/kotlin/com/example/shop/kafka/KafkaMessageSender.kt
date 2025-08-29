package com.example.shop.kafka

import com.example.shop.auth.security.events.models.AutoRegisteredAccountEvent
import com.example.shop.constants.AUTO_REGISTERED_ACCOUNT_TOPIC
import com.example.shop.constants.NOTIFY_ADMIN_REFUND_TOPIC
import com.example.shop.constants.NOTIFY_USER_REFUND_TOPIC
import com.example.shop.refund.models.RefundKafkaDto
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaMessageSender(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    fun sendAutoRegisterMessage(autoRegisteredAccountEvent: AutoRegisteredAccountEvent) {
        // KafkaTemplate은 내부적으로 KSerializerKafkaSerializer를 호출하여 객체를 직렬화합니다.
        kafkaTemplate.send(AUTO_REGISTERED_ACCOUNT_TOPIC, autoRegisteredAccountEvent)
    }

    fun sendRefundMessageToAdmin(refundKafkaDto: RefundKafkaDto) {
        kafkaTemplate.send(NOTIFY_ADMIN_REFUND_TOPIC, refundKafkaDto)
    }

    fun sendRefundMessageToUser(refundKafkaDto: RefundKafkaDto) {
        kafkaTemplate.send(NOTIFY_USER_REFUND_TOPIC, refundKafkaDto)
    }
}
