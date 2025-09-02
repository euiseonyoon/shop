package com.example.shop.kafka.notify_topic.message_handlers

import com.example.shop.kafka.KafkaMessageHandler
import com.example.shop.kafka.notify_topic.models.NotifyKafkaContent
import com.example.shop.kafka.notify_topic.models.NotifyKafkaMessage
import org.springframework.stereotype.Component

@Component
class NotifyKafkaMessageHandler : KafkaMessageHandler<NotifyKafkaMessage> {
    override fun handleMessage(message: NotifyKafkaMessage) {
        when(message.content) {
            is NotifyKafkaContent.RefundKafkaDto -> {
                // TODO
            }
            is NotifyKafkaContent.AutoRegisteredAccountKafkaDto -> {
                // TODO
            }
        }
    }
}
