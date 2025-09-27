package com.example.shop.admin.models.account

data class AdminAccountUpdateRequest(
    val accountId: Long,
    val enabled: Boolean?,
    val authorityName: String?,
    val addGroupIds: List<Long>?,
    val removeGroupIds: List<Long>?,
)
