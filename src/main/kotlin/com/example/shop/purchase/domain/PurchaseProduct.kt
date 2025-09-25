package com.example.shop.purchase.domain

import com.example.shop.products.domain.Product
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.validation.constraints.Min

@Entity
@Table(
    indexes = [
        Index(name = "idx_purchase_product_purchase_id", columnList = "purchase_id"),
        Index(name = "idx_purchase_product_product_id", columnList = "purchase_id")
    ]
)
class PurchaseProduct {
    @Id
    @SequenceGenerator(name = "purchase_product_seq_gen", sequenceName = "purchase_product_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchase_product_seq_gen")
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    var purchase: Purchase? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null

    @Min(1)
    @Column(name = "count", nullable = false)
    var quantity: Int? = null

    companion object {
        fun create(product: Product, quantity: Int): PurchaseProduct {
            return PurchaseProduct().apply {
                this.product = product
                this.quantity = quantity
            }
        }
    }
}
