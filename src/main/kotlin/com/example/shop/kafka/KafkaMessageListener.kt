package com.example.shop.kafka

import com.example.shop.common.logger.LogSupport
import com.example.shop.constants.NOTIFY_DLQ_TOPIC
import com.example.shop.constants.NOTIFY_TOPIC
import com.example.shop.constants.PRODUCT_STOCK_UPDATE_TOPIC
import com.example.shop.kafka.notify_topic.models.NotifyKafkaContent
import com.example.shop.kafka.notify_topic.models.NotifyKafkaMessage
import com.example.shop.kafka.product_stock_topic.models.ProductStockUpdateKafkaMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class KafkaMessageListener(
    private val notifyMsgHandler: KafkaMessageHandler<NotifyKafkaMessage>,
    private val productStockUpdateMsgHandler: KafkaMessageHandler<ProductStockUpdateKafkaMessage>,
    private val kafkaMessageSender: KafkaMessageSender
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

        val notifyKafkaMessage = try {
            json.decodeFromString<NotifyKafkaMessage>(jsonString)
        } catch (e: Exception) {
            logger.error("Failed to decode notify kafka message. e={}, messageByte={}", e, messageBytes)
            ack.acknowledge()
            return
        }

        try {
            notifyMsgHandler.handleMessage(notifyKafkaMessage)
        } catch (e: Exception) {
            logger.error("Kafka notify 메시지 처리 오류. e={}", e)
            kafkaMessageSender.sendNotifyDlqMessage(notifyKafkaMessage)
        } finally {
            ack.acknowledge()
        }
    }

    @KafkaListener(
        topics = [NOTIFY_DLQ_TOPIC],
        groupId = "notify_dlq_group",
        containerFactory = "notifyTopicListenerContainerFactory"
    )
    fun listenNotifyDlqKafka(messageBytes: ByteArray, ack: Acknowledgment) {
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

        val notifyKafkaMessage = try {
            json.decodeFromString<NotifyKafkaMessage>(jsonString)
        } catch (e: Exception) {
            logger.error("Failed to decode notify kafka message. e={}, messageByte={}", e, messageBytes)
            ack.acknowledge()
            return
        }

        try {
            notifyMsgHandler.handleMessage(notifyKafkaMessage)
        } catch (e: Exception) {
            logger.error("Kafka notify 메시지 처리 오류. e={}", e)
            kafkaMessageSender.saveNotifyMessageOnDb(notifyKafkaMessage)
        } finally {
            ack.acknowledge()
        }
    }

    @KafkaListener(
        topics = [PRODUCT_STOCK_UPDATE_TOPIC],
        groupId = "product_stock_update_group",
        containerFactory = "productStockUpdateListenerContainerFactory"
    )
    fun listenProductStockUpdateKafka(messageBytes: ByteArray, ack: Acknowledgment) {
        val jsonString = String(messageBytes, StandardCharsets.UTF_8)
        val json = Json

        val productStockUpdateMsg = try {
            json.decodeFromString<ProductStockUpdateKafkaMessage>(jsonString)
        } catch (e: Exception) {
            logger.error("Failed to decode product stock update kafka message. e={}, messageByte={}", e, messageBytes)
            ack.acknowledge()
            return
        }

        productStockUpdateMsgHandler.handleMessage(productStockUpdateMsg)
        ack.acknowledge()
    }
}
