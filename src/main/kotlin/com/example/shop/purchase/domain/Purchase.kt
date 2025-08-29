package com.example.shop.purchase.domain

import com.example.shop.auth.domain.Account
import com.example.shop.purchase.enums.PurchaseStatus
import com.example.shop.refund.domain.Refund
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Entity
class Purchase {
    @Id
    @GeneratedValue
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    var account: Account? = null

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()

    var updatedAt: OffsetDateTime? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PurchaseStatus = PurchaseStatus.PAID

    @OneToMany(mappedBy = "purchase", cascade = [CascadeType.ALL])
    var purchaseProducts: MutableSet<PurchaseProduct> = mutableSetOf()

    @OneToOne(mappedBy = "purchase", fetch = FetchType.LAZY)
    var refund: Refund? = null

    fun addPurchaseProducts(purchaseProducts: List<PurchaseProduct>) {
        this.purchaseProducts.addAll(purchaseProducts)
        purchaseProducts.map {
            if (it.purchase != this) {
                it.purchase = this
            }
        }
    }
}
