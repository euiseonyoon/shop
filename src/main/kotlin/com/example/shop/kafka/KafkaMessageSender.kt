package com.example.shop.kafka

import com.example.shop.common.logger.LogSupport
import com.example.shop.constants.NOTIFY_TOPIC
import com.example.shop.kafka.domain.NotifyMessage
import com.example.shop.kafka.notify_topic.models.NotifyKafkaContent
import com.example.shop.kafka.notify_topic.models.NotifyKafkaMessage
import com.example.shop.kafka.repositories.NotifyMessageRepository
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaMessageSender(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val notifyMessageRepository: NotifyMessageRepository
): LogSupport() {
    private fun saveNotifyMessageOnDb(message: NotifyKafkaMessage) {
        val notifyMessageEntity = NotifyMessage()
        notifyMessageEntity.type = message.type

        when (message.content) {
            is NotifyKafkaContent.AutoRegisteredAccountKafkaDto -> {
                notifyMessageEntity.autoRegisteredAccountEmail = message.content.email
            }
            is NotifyKafkaContent.RefundKafkaDto -> {
                notifyMessageEntity.refundId = message.content.refundId
            }
        }

        notifyMessageRepository.save(notifyMessageEntity)
    }

    fun sendNotifyMessage(message: NotifyKafkaMessage) {
        try {
            // partition key를 지정하지 않고 보냄 -> 해당 토픽의 여러 파티션에 골고루 분산 (메시지의 순서보장은 파티션내에서 만 이루어 지기 때문에
            // 전체 메시지의 순서보장이 되지는 않는다)
            kafkaTemplate.send(NOTIFY_TOPIC, message)
            // 아래처럼 해야, 특정 파티션에 특정 메시지가 저장되어 순서보장이 된다.
            // kafkaTemplate.send(NOTIFY_TOPIC, someKey, message)

            // 결론: 모든 메시지의 전역적인 순서보장이 필요하다면 토픽은 1개의 파티션만으로 이루어 져야 한다.
            // 하지만 notify_topic의 경우, 전역적인 순서보장이 굳이 필요없으므로 partition 수를 3개로 구성하여
            // 병렬처리 한다.
        } catch (e: Exception) {
            logger.error("Failed to send notify kafka message. e={}, message={}", e, message)
            saveNotifyMessageOnDb(message)
        }
    }
}
