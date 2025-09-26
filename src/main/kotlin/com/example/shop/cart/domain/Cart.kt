package com.example.shop.cart.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    indexes = [
        Index(name = "idx_cart_account_id", columnList = "account_id"),
        Index(name = "idx_cart_is_purchased", columnList = "is_purchased"),
    ]
)
class Cart(
    @Column(name = "account_id", nullable = false)
    val accountId: Long,

    @Column(name = "is_purchased")
    var isPurchased: Boolean = false
) {
    @Id @GeneratedValue
    val id: Long = 0
}
