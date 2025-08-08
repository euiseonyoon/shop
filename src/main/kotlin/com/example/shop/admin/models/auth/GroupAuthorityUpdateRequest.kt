package com.example.shop.admin.models.auth

import com.example.shop.common.apis.annotations.AtLeastOneNotNull

@AtLeastOneNotNull(message = "name 또는 groupName 중 하나는 null이 아니어야 합니다.")
data class GroupAuthorityUpdateRequest(
    val id: Long,
    val name: String?,
    val groupName: String?,
)
