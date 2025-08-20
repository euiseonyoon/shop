package com.example.shop.account.domain

import com.example.shop.auth.domain.Account
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    indexes = [
        Index(name = "idx_account_id", columnList = "account_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["account_id", "description"])
    ],
)
class Address {
    @Id
    @GeneratedValue
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    var account: Account? = null

    @Column(nullable = false)
    var description: String? = null

    @Column(nullable = false)
    var detail: String? = null
}
