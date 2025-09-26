package com.example.shop.auth.domain.extension_functions

import com.example.shop.auth.domain.Account
import com.example.shop.common.apis.models.AdminAccountDto
import com.example.shop.common.apis.models.AdminAccountDto.GroupInfo
import com.example.shop.common.apis.models.AuthorityDto

fun Account.toAdminAccountDto(): AdminAccountDto {
    val groupAndGroupAuthoritiesMap = groupAuthorities.groupBy { it.accountGroup }
    val groups = accountGroups.map { accountGroup ->
        if (groupAndGroupAuthoritiesMap[accountGroup] != null) {
            val authorities = groupAndGroupAuthoritiesMap[accountGroup]!!.map {
                AdminAccountDto.MinimumGroupAuthorityDto(it.id, it.name)
            }
            GroupInfo(accountGroup.toDto(), authorities)
        } else {
            GroupInfo(accountGroup.toDto(), emptyList())
        }
    }

    return AdminAccountDto(
        id = this.id,
        email = this.email,
        enabled = this.enabled,
        nickname = this.nickname,
        authority = AuthorityDto(this.authority.id, this.authority.roleName, this.authority.hierarchy),
        groups = groups
    )
}
