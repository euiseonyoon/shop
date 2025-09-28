package com.example.shop.auth.domain.extension_functions

import com.example.shop.auth.domain.AccountDomain
import com.example.shop.common.apis.models.AccountGroupDto
import com.example.shop.common.apis.models.AdminAccountDto
import com.example.shop.common.apis.models.AdminAccountDto.GroupInfo
import com.example.shop.common.apis.models.AdminAccountDto.MinimumGroupAuthorityDto
import com.example.shop.common.apis.models.AuthorityDto

fun AccountDomain.toAdminAccountDto(): AdminAccountDto {
    val groups = this.accountGroupMap.map { (accountGroup, groupAuthorityList) ->
        val key = AccountGroupDto(accountGroup.id, accountGroup.name)
        val value = groupAuthorityList.map { MinimumGroupAuthorityDto(it.id, it.role.name) }
        key to value
    }.toMap().map { (k, v) -> GroupInfo(k, v) }

    return AdminAccountDto(
        id = this.account.id,
        email = this.account.email,
        enabled = this.account.enabled,
        nickname = this.account.nickname,
        authority = AuthorityDto(this.authority.id, this.authority.role.name, this.authority.hierarchy),
        groups = groups
    )
}
