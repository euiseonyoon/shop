package com.example.shop.kafka.domain

import com.example.shop.kafka.notify_topic.enums.NotifyType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class NotifyMessage {
    @Id
    @GeneratedValue
    val id: Long? = null

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    var type: NotifyType? = null

    @Column(nullable = false)
    var autoRegisteredAccountEmail: String? = null

    @Column(nullable = true)
    var refundId: Long? = null
}
