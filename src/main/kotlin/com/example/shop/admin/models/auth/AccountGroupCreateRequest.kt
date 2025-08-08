package com.example.shop.admin.models.auth

import jakarta.validation.constraints.NotBlank

data class AccountGroupCreateRequest(
    @field:NotBlank
    val name: String,
)
