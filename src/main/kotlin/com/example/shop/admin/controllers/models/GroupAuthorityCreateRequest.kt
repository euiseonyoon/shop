package com.example.shop.admin.controllers.models

import jakarta.validation.constraints.NotBlank

data class GroupAuthorityCreateRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val groupName: String,
)
