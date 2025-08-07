package com.example.shop.admin.controllers.models

import jakarta.validation.constraints.NotEmpty

data class GroupAuthorityDeleteRequest(
    @field:NotEmpty
    val ids: List<Long>
)
