package com.example.shop.admin.models.auth

import jakarta.validation.constraints.NotBlank

data class AccountGroupUpdateRequest(
    val id: Long,

    @field:NotBlank
    val name: String,
)
