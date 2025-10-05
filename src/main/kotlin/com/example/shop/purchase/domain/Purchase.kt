package com.example.shop.purchase.domain

import com.example.shop.common.domain.AuditEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.validation.constraints.Min
import org.hibernate.annotations.NaturalId
import java.util.UUID

@Entity
class Purchase(
    @Column(nullable = false)
    val accountId: Long,

    @Column(nullable = false) @Min(0)
    val totalPrice: Int,
) : AuditEntity() {
    @Id @GeneratedValue
    val id: Long = 0

    @Column(nullable = false) @NaturalId
    val uuid: String = UUID.randomUUID().toString()
}
