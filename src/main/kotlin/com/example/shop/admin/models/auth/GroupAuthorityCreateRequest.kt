package com.example.shop.admin.models.auth

import jakarta.validation.constraints.NotBlank

data class GroupAuthorityCreateRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val groupName: String,
)
