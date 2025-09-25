package com.example.shop.common.domain

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.OffsetDateTime

@MappedSuperclass
abstract class AuditEntity {
    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column
    var updatedAt: OffsetDateTime? = null
}
