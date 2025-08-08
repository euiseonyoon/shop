package com.example.shop.admin.models.auth

import jakarta.validation.constraints.NotEmpty

data class GroupAuthorityDeleteRequest(
    @field:NotEmpty
    val ids: List<Long>
)
