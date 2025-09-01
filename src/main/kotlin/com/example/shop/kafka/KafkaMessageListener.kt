package com.example.shop.kafka

import com.example.shop.auth.security.kafka.models.AutoRegisteredAccountKafkaDto
import com.example.shop.constants.AUTO_REGISTERED_ACCOUNT_TOPIC
import com.example.shop.constants.NOTIFY_ADMIN_REFUND_TOPIC
import com.example.shop.constants.NOTIFY_USER_REFUND_TOPIC
import com.example.shop.refund.models.RefundKafkaDto
import kotlinx.serialization.json.Json
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class KafkaMessageListener(
    private val json: Json // 역직렬화를 위해 Json 객체 주입
) {
    @KafkaListener(
        topics = [NOTIFY_ADMIN_REFUND_TOPIC],
        groupId = "notify_admin_refund_group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun notifyAdminAboutRefund(messageBytes: ByteArray) {
        val jsonString = String(messageBytes, StandardCharsets.UTF_8)
        val refundInfo = try {
            json.decodeFromString(RefundKafkaDto.serializer(), jsonString)
        } catch (e: Exception) {
            throw e
        }
        // TODO: 어드민에게 이메일이던 뭐던 notify한다
    }

    @KafkaListener(
        topics = [NOTIFY_USER_REFUND_TOPIC],
        groupId = "notify_user_refund_group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun notifyUserAboutRefund(messageBytes: ByteArray) {
        val jsonString = String(messageBytes, StandardCharsets.UTF_8)
        val refundInfo = try {
            json.decodeFromString(RefundKafkaDto.serializer(), jsonString)
        } catch (e: Exception) {
            throw e
        }
        // TODO: 유저에게 이메일이던 뭐던 notify한다
    }

    @KafkaListener(
        topics = [AUTO_REGISTERED_ACCOUNT_TOPIC],
        groupId = "auto_registered_account_group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleAutoRegisteredAccount(messageBytes: ByteArray) {
        val jsonString = String(messageBytes, StandardCharsets.UTF_8)
        val autoRegisteredUserInfo = try {
            json.decodeFromString(AutoRegisteredAccountKafkaDto.serializer(), jsonString)
        } catch (e: Exception) {
            throw e
        }
        // TODO: 유저에게 자동 회원가입이 되었다고 이메일을 보낸다.
    }
}
