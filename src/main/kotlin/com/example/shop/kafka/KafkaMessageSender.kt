package com.example.shop.kafka

import com.example.shop.auth.security.kafka.models.AutoRegisteredAccountKafkaDto
import com.example.shop.common.logger.LogSupport
import com.example.shop.constants.AUTO_REGISTERED_ACCOUNT_TOPIC
import com.example.shop.constants.NOTIFY_ADMIN_REFUND_TOPIC
import com.example.shop.constants.NOTIFY_USER_REFUND_TOPIC
import com.example.shop.refund.models.RefundKafkaDto
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaMessageSender(
    private val kafkaTemplate: KafkaTemplate<String, Any>
): LogSupport() {
    fun sendAutoRegisterMessage(autoRegisteredAccountKafkaDto: AutoRegisteredAccountKafkaDto) {
        // KafkaTemplate은 내부적으로 KSerializerKafkaSerializer를 호출하여 객체를 직렬화합니다.
        logger.info("Send kafka Auto registered user info message. email={}", autoRegisteredAccountKafkaDto.email)
        kafkaTemplate.send(AUTO_REGISTERED_ACCOUNT_TOPIC, autoRegisteredAccountKafkaDto)
    }

    fun sendRefundMessageToAdmin(refundKafkaDto: RefundKafkaDto) {
        kafkaTemplate.send(NOTIFY_ADMIN_REFUND_TOPIC, refundKafkaDto)
    }

    fun sendRefundMessageToUser(refundKafkaDto: RefundKafkaDto) {
        kafkaTemplate.send(NOTIFY_USER_REFUND_TOPIC, refundKafkaDto)
    }
}
