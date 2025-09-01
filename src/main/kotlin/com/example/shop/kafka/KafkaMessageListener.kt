package com.example.shop.kafka

import com.example.shop.auth.security.kafka.models.AutoRegisteredAccountKafkaDto
import com.example.shop.constants.AUTO_REGISTERED_ACCOUNT_TOPIC
import com.example.shop.constants.NOTIFY_ADMIN_REFUND_TOPIC
import com.example.shop.constants.NOTIFY_USER_REFUND_TOPIC
import com.example.shop.refund.models.RefundKafkaDto
import kotlinx.serialization.json.Json
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
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
    fun notifyAdminAboutRefund(messageBytes: ByteArray, ack: Acknowledgment) {
        try {
            val jsonString = String(messageBytes, StandardCharsets.UTF_8)
            val refundInfo = json.decodeFromString(RefundKafkaDto.serializer(), jsonString)
            // TODO: 어드민에게 이메일이던 뭐던 notify한다.

            // 메시지 처리가 성공했으므로 오프셋을 커밋합니다.
            ack.acknowledge()
        } catch (e: Exception) {
            // 역직렬화 또는 비즈니스 로직 처리 중 오류 발생 시
            // ack.acknowledge()를 호출하지 않아 다음 폴링 시 메시지를 다시 받습니다.
            throw e
        }
    }

    @KafkaListener(
        topics = [NOTIFY_USER_REFUND_TOPIC],
        groupId = "notify_user_refund_group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun notifyUserAboutRefund(messageBytes: ByteArray, ack: Acknowledgment) {
        try {
            val jsonString = String(messageBytes, StandardCharsets.UTF_8)
            val refundInfo = json.decodeFromString(RefundKafkaDto.serializer(), jsonString)
            ack.acknowledge()
            // TODO: 유저에게 이메일이던 뭐던 notify한다
        } catch (e: Exception) {
            throw e
        }
    }

    @KafkaListener(
        topics = [AUTO_REGISTERED_ACCOUNT_TOPIC],
        groupId = "auto_registered_account_group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleAutoRegisteredAccount(messageBytes: ByteArray, ack: Acknowledgment) {
        try {
            val jsonString = String(messageBytes, StandardCharsets.UTF_8)
            val autoRegisteredUserInfo = json.decodeFromString(AutoRegisteredAccountKafkaDto.serializer(), jsonString)
            // TODO: 유저에게 자동 회원가입이 되었다고 이메일을 보낸다.
            ack.acknowledge()
        } catch (e: Exception) {
            throw e
        }
    }
}
