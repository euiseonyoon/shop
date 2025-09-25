package com.example.shop.refund.domain

import com.example.shop.common.domain.AuditEntity
import com.example.shop.refund.enums.RefundStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    indexes = [
        Index(name = "idx_purchase_id", columnList = "purchase_id"),
    ],
)
class Refund(
    @Column(name = "purchase_id", nullable = false, unique = true)
    val purchaseId: Long,

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    var status: RefundStatus = RefundStatus.REQUESTED,

    @Column
    val reason: String? = null,

    // 만약 환불거절될떄나 특정 내용을 남겨야 할 때 사용.
    @Column
    var etc: String? = null,

): AuditEntity() {
    @Id @GeneratedValue
    val id: Long = 0
}
