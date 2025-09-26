package com.example.shop.purchase.domain

import com.example.shop.common.domain.AuditEntity
import com.example.shop.purchase.enums.PurchaseStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class Purchase(
    @Column(nullable = false)
    val accountId: Long,

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    var status: PurchaseStatus = PurchaseStatus.PAID,

) : AuditEntity() {
    @Id @GeneratedValue
    val id: Long = 0
}
