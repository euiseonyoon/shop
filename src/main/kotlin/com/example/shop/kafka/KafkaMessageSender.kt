package com.example.shop.kafka

import com.example.shop.common.logger.LogSupport
import com.example.shop.constants.NOTIFY_TOPIC
import com.example.shop.kafka.notify_topic.models.NotifyKafkaMessage
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaMessageSender(
    private val kafkaTemplate: KafkaTemplate<String, Any>
): LogSupport() {
    fun sendNotifyMessage(message: NotifyKafkaMessage) {
        kafkaTemplate.send(NOTIFY_TOPIC, message)
    }
}
