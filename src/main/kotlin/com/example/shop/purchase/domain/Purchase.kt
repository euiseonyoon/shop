package com.example.shop.purchase.domain

import com.example.shop.common.domain.AuditEntity
import com.example.shop.purchase.enums.PurchaseStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.validation.constraints.Min
import org.hibernate.annotations.NaturalId
import java.time.OffsetDateTime
import java.util.UUID

@Entity
class Purchase(
    @Column(nullable = false)
    val accountId: Long,

    @Column(nullable = false) @Min(0)
    val totalPrice: Int,

    @Column
    val cartId: Long? = null
) : AuditEntity() {
    @Id @GeneratedValue
    val id: Long = 0

    @Column(nullable = false) @NaturalId
    val uuid: String = UUID.randomUUID().toString()

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    var status: PurchaseStatus = PurchaseStatus.READY

    fun updateStatus(status: PurchaseStatus) {
        this.status = status
        this.updatedAt = OffsetDateTime.now()
    }
}
