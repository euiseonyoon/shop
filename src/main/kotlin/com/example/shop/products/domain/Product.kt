package com.example.shop.products.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
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
        Index(name = "idx_product_stock", columnList = "stock"),
        Index(name = "idx_product_price", columnList = "price"),
        Index(name = "idx_product_category", columnList = "category"),
        Index(name = "idx_product_is_enabled", columnList = "is_enabled")
    ]
)
class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq_gen")
    @SequenceGenerator(
        name = "product_seq_gen",
        sequenceName = "product_id_seq",
        allocationSize = 1
    )
    val id: Long? = null

    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false)
    @Min(0)
    var stock: Int? = null

    @Column(nullable = false)
    @Min(0)
    var price: Int? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    var category: Category? = null

    @Column(nullable = false)
    var isEnabled: Boolean = true
}
