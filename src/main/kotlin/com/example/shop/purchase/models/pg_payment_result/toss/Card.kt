package com.example.shop.purchase.models.pg_payment_result.toss

data class Card(
    val cardCompany: String,
    val number: String?,
    val installmentPlanMonths: Int?,
    val approveNo: String?,
    val useCardPoint: Boolean?,
    val interestFreeInstall: Boolean?,
    val isInterestFree: Boolean?
)