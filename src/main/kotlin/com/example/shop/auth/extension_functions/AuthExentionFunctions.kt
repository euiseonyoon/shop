package com.example.shop.auth.extension_functions

import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.Authority
import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.common.apis.models.AccountGroupDto
import com.example.shop.common.apis.models.AuthorityDto
import com.example.shop.common.apis.models.GroupAuthorityDto

fun Authority.toAuthorityDto(): AuthorityDto {
    return AuthorityDto(
        this.id!!,
        this.roleName!!,
        this.hierarchy!!,
    )
}

fun AccountGroup.toAccountGroupDto(): AccountGroupDto {
    return AccountGroupDto(this.id!!, this.name!!)
}

fun GroupAuthority.toGroupAuthorityDto(): GroupAuthorityDto {
    return GroupAuthorityDto(
        this.id!!,
        this.name!!,
        this.accountGroup!!.id!!,
        this.accountGroup!!.name!!
    )
}
