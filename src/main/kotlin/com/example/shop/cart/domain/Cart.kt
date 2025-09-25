package com.example.shop.cart.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(
    indexes = [
        Index(name = "idx_cart_account_id", columnList = "account_id"),
        Index(name = "idx_cart_is_purchased", columnList = "is_purchased"),
    ]
)
class Cart(
    @Id
    @GeneratedValue
    val id: Long = 0,

    @Column(name = "account_id", nullable = false)
    val accountId: Long,

    @Column(name = "is_purchased")
    var isPurchased: Boolean = false
) {
    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val cartItems: MutableSet<CartItem> = mutableSetOf()

    val items: List<CartItem>
        get() = cartItems.toList()

    fun findItem(productId: Long): CartItem? = cartItems.find { it.productId == productId }

    fun addItem(productId: Long, quantity: Int) {
        cartItems.add(
            CartItem(
                cart = this,
                productId = productId,
                quantity = quantity,
            )
        )
    }

    fun removeItem(cartItemToRemove: CartItem) {
        cartItems.remove(cartItemToRemove)
    }

    fun setItemQuantity(cartItem: CartItem, quantity: Int) {
        cartItem.quantity = quantity
    }

    fun incrementItemQuantity(cartItem: CartItem, quantity: Int) {
        cartItem.quantity += quantity
    }
}
