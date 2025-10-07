package com.example.shop.kafka

import com.example.shop.constants.NOTIFY_DLQ_TOPIC
import com.example.shop.constants.NOTIFY_TOPIC
import com.example.shop.constants.PRODUCT_STOCK_UPDATE_TOPIC
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import kotlin.reflect.full.hasAnnotation


@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val kafkaBootstrapServers: String,
    private val json: Json,
) {
    // NOTE: spring boot를 사용하면 kafkaAdmin은 자동으로 빈으로 등록된다.
    // ListenerContainer는 consumer를 사용해서 kafka 로부터 메시지를 계속 polling 해주는 역할을 한다.

    val NOTIFY_PARTITION_COUNT = 3
    val PRODUCT_PARTITION_COUNT = 32

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun producerFactory(kSerializerKafkaSerializer: Serializer<Any>): ProducerFactory<String, Any> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBootstrapServers

        // --- Kafka Producer 설정 추가 ---
        // 메시지 전송 성공으로 간주하기 위한 응답 수준을 'all'로 설정하여 가장 높은 안정성을 확보합니다.
        configProps[ProducerConfig.ACKS_CONFIG] = "all"
        // 일시적인 네트워크 오류에 대비해 재시도 횟수를 3번으로 설정합니다.
        configProps[ProducerConfig.RETRIES_CONFIG] = 3
        // 재시도 간격을 1초(1000ms)로 설정합니다.
        configProps[ProducerConfig.RETRY_BACKOFF_MS_CONFIG] = 1000L
        // --- 설정 끝 ---

        return DefaultKafkaProducerFactory(configProps, StringSerializer(), kSerializerKafkaSerializer)
    }

    @OptIn(InternalSerializationApi::class)
    @Bean
    fun kSerializerKafkaSerializer(): Serializer<Any> {
        return object : Serializer<Any> {
            override fun serialize(topic: String, data: Any): ByteArray {
                return try {
                    // @Serializable 어노테이션이 있는지 확인하여 오류를 사전에 방지합니다.
                    if (!data::class.hasAnnotation<Serializable>()) {
                        throw IllegalArgumentException("Data class must be annotated with @Serializable")
                    }
                    // Dynamically get the KSerializer for the specific data type
                    val kSerializer = data::class.serializer() as KSerializer<Any>
                    json.encodeToString(kSerializer, data).toByteArray()
                } catch (e: Exception) {
                    // Handle serialization errors
                    throw RuntimeException("Failed to serialize data: $data", e)
                }
            }
        }
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, ByteArray> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBootstrapServers
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java

        // --- 오프셋 자동 커밋 설정 (기본값) ---
        configProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        // --- 설정 끝 ---

        return DefaultKafkaConsumerFactory(configProps)
    }

    // ==============================================================================
    // 1. 알림/공지 토픽 리스너 팩토리 (Concurrency: 3)
    // ==============================================================================
    @Bean
    fun notifyTopicListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, ByteArray>
    ): ConcurrentKafkaListenerContainerFactory<String, ByteArray> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, ByteArray>()

        // 컨테이너의 ACK 모드를 수동(MANUAL)으로 설정
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.consumerFactory = consumerFactory
        // MSA 환경에서는 조정을 할 필요가 있다.
        // 현재 코드베이스에서 3개의 컨수머가 1개의 컨수머 그룹을 만든다. 그래서 아래의 notifyTopic의 partition 수와 동일해서 상관없다.
        // 하지만 MSA 환경에서 이러한 서버가 2대 실행된다면  서버당 3개의 컨수머  총  6개의 컨수머가 만들어진다.
        // 하지만 notifyTopic의 파티션은 3개이므로 3개의 컨수머는 idle이다.
        factory.setConcurrency(NOTIFY_PARTITION_COUNT)
        return factory
    }

    // ==============================================================================
    // 2. 재고 업데이트 토픽 리스너 팩토리 (Concurrency: 32)
    // ==============================================================================
    /**
     * 상품 재고 업데이트 토픽 전용 리스너 팩토리.
     * PRODUCT_PARTITION_COUNT(32)와 동일하게 Concurrency를 설정하여
     * '파티션 당 하나의 컨슈머'가 할당되도록 보장합니다.
     */
    @Bean
    fun productStockUpdateListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, ByteArray>
    ): ConcurrentKafkaListenerContainerFactory<String, ByteArray> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, ByteArray>()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.consumerFactory = consumerFactory

        // PRODUCT_STOCK_UPDATE_TOPIC의 파티션 수(32)와 동일하게 컨슈머를 생성합니다.
        // 이 서버 인스턴스에서 32개의 컨슈머가 생성되며, 이는 32개의 파티션을 정확히 1:1로 담당합니다.
        factory.setConcurrency(PRODUCT_PARTITION_COUNT)

        return factory
    }

    // ==============================================================================
    // Topic Definitions
    // ==============================================================================
    @Bean
    fun notifyTopic(): NewTopic {
        return TopicBuilder.name(NOTIFY_TOPIC)
            .partitions(NOTIFY_PARTITION_COUNT)
            .replicas(3)
            .build()
    }

    @Bean
    fun productStockUpdateTopic(): NewTopic {
        return TopicBuilder.name(PRODUCT_STOCK_UPDATE_TOPIC)
            .partitions(PRODUCT_PARTITION_COUNT)
            .replicas(3)
            .build()
    }

    @Bean
    fun notifyDlqTopic(): NewTopic {
        return TopicBuilder.name(NOTIFY_DLQ_TOPIC)
            .partitions(NOTIFY_PARTITION_COUNT)
            .replicas(3)
            .build()
    }
}
