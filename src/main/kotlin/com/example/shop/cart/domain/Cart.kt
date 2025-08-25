package com.example.shop.cart.domain

import com.example.shop.auth.domain.Account
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(
    indexes = [
        Index(name = "idx_cart_account_id", columnList = "account_id"),
        Index(name = "idx_cart_is_purchased", columnList = "is_purchased"),
    ]
)
class Cart {
    @Id
    @GeneratedValue
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    var account: Account? = null

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    var cartItems: MutableSet<CartItem>? = mutableSetOf()

    var isPurchased: Boolean = false
}
