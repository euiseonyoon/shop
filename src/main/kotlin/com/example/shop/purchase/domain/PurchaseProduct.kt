package com.example.shop.purchase.domain

import com.example.shop.purchase.enums.PurchaseProductStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.validation.constraints.Min

@Entity
@Table(
    indexes = [
        Index(name = "idx_purchase_product_purchase_id", columnList = "purchase_id"),
        Index(name = "idx_purchase_product_product_id", columnList = "product_id")
    ]
)
class PurchaseProduct(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val purchase: Purchase,

    @Column(nullable = false)
    val productId: Long,

    @Column(nullable = false) @Min(1)
    val count: Int,

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    var status: PurchaseProductStatus = PurchaseProductStatus.READY
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchase_product_seq_gen")
    @SequenceGenerator(name = "purchase_product_seq_gen", sequenceName = "purchase_product_id_seq", allocationSize = 1)
    val id: Long = 0
}
