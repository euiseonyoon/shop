package com.example.shop.kafka

import com.example.shop.common.logger.LogSupport
import com.example.shop.constants.NOTIFY_TOPIC
import com.example.shop.kafka.notify_topic.models.NotifyKafkaContent
import com.example.shop.kafka.notify_topic.models.NotifyKafkaMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class KafkaMessageListener(
    private val kafkaMessageHandler: KafkaMessageHandler<NotifyKafkaMessage>,
) : LogSupport() {
    @KafkaListener(
        topics = [NOTIFY_TOPIC],
        groupId = "notify_group",
        containerFactory = "notifyTopicListenerContainerFactory"
    )
    fun listenNotifyKafka(messageBytes: ByteArray, ack: Acknowledgment) {
        val jsonString = String(messageBytes, StandardCharsets.UTF_8)
        val json = Json {
            serializersModule = SerializersModule {
                polymorphic(NotifyKafkaContent::class) {
                    subclass(
                        NotifyKafkaContent.AutoRegisteredAccountKafkaDto::class,
                        NotifyKafkaContent.AutoRegisteredAccountKafkaDto.serializer()
                    )
                    subclass(
                        NotifyKafkaContent.RefundKafkaDto::class,
                        NotifyKafkaContent.RefundKafkaDto.serializer()
                    )
                }
            }
        }

        try {
            val notifyKafkaMessage = json.decodeFromString<NotifyKafkaMessage>(jsonString)
            kafkaMessageHandler.handleMessage(notifyKafkaMessage)
            ack.acknowledge()
        } catch (e: Exception) {
            logger.error("Kafka notify 메시지 처리 오류. e={}", e.message)
        }
    }
}
