package com.example.shop.admin.controllers.models

import com.example.shop.constants.ROLE_PREFIX
import jakarta.validation.constraints.Pattern

data class AuthorityCreateRequest(
    @field:Pattern(regexp = "^${ROLE_PREFIX}[A-Z_0-9]+\$", message = "Role name must start with '${ROLE_PREFIX}'")
    val name: String,
    val hierarchy: Int,
)
