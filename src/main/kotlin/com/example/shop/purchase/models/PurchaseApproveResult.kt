package com.example.shop.purchase.models

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
