package com.example.shop.products.domain

import com.example.shop.products.exceptions.ProductInsufficientStockException
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
        Index(name = "idx_product_category", columnList = "category_id"),
        Index(name = "idx_product_is_enabled", columnList = "is_enabled")
    ]
)
class Product(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false) @Min(0)
    var stock: Int,

    @Column(nullable = false) @Min(0)
    var price: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @Column(nullable = false)
    var isEnabled: Boolean = true
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq_gen")
    @SequenceGenerator(
        name = "product_seq_gen",
        sequenceName = "product_id_seq",
        allocationSize = 1
    )
    val id: Long = 0

    fun decrementStock(quantity: Int): Product {
        require(quantity >= 0)
        if (isStockInsufficient(quantity)) {
            throw ProductInsufficientStockException("상품 수량이 부족합니다.")
        }
        this.stock -= quantity
        return this
    }

    fun incrementStock(quantity: Int): Product {
        require(quantity >= 0)
        this.stock += quantity
        return this
    }

    fun isStockInsufficient(quantity: Int): Boolean = this.stock < quantity

    fun isPurchasable(quantity: Int?): Pair<Boolean, String> {
        if (quantity != null) {
            if (isStockInsufficient(quantity)){
                return Pair(false, "상품 재고 수량 부족")
            } else {
                return Pair(true, "")
            }
        }

        return when {
            this.isEnabled -> Pair(true, "")
            else -> Pair(false, "해당 상품 비활성화")
        }
    }
}
