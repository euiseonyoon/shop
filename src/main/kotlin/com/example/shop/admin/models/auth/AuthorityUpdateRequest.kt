package com.example.shop.admin.models.auth

data class AuthorityUpdateRequest(
    val id: Long,
    val hierarchy: Int,
)
