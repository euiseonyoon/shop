package com.example.shop.admin.controllers.models

data class AdminAccountUpdateRequest(
    val accountId: Long,
    val enabled: Boolean?,
    val authorityName: String?,
    val addGroupNames: List<String>?,
    val removeGroupNames: List<String>?,
)
