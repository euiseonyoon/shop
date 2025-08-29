package com.example.shop.configurations.kafka

import com.example.shop.auth.security.events.models.AutoRegisteredAccountEvent
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import kotlin.reflect.full.hasAnnotation


@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val kafka_bootstrap_servers: String,
    private val json: Json,
) {
    // NOTE: spring boot를 사용하면 kafkaAdmin은 자동으로 빈으로 등록된다.
    // ListenerContainer는 consumer를 사용해서 kafka 로부터 메시지를 계속 polling 해주는 역할을 한다.

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun producerFactory(kSerializerKafkaSerializer: Serializer<Any>): ProducerFactory<String, Any> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafka_bootstrap_servers

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
        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafka_bootstrap_servers
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java

        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, ByteArray>
    ): ConcurrentKafkaListenerContainerFactory<String, ByteArray> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, ByteArray>()
        factory.consumerFactory = consumerFactory
        return factory
    }
}
