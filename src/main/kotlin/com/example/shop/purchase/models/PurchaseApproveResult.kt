package com.example.shop.purchase.models

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseApproveResult(
    val isApproved: Boolean,
    val disapprovedReason: String?
) {
    init {
        if (!isApproved) {
            require(disapprovedReason != null)
        }
    }
}
