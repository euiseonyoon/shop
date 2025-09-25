package com.example.shop.cart.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.Min

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["cart_id", "product_id"])
    ]
)
class CartItem(
    @Id
    @GeneratedValue
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    val cart: Cart,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @Min(1)
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
)
