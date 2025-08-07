package com.example.shop.admin.controllers.models

import jakarta.validation.constraints.NotBlank

data class AccountGroupUpdateRequest(
    val id: Long,

    @field:NotBlank
    val name: String,
)
