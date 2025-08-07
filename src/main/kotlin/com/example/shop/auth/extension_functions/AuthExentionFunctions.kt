package com.example.shop.auth.extension_functions

import com.example.shop.auth.domain.Authority
import com.example.shop.common.apis.models.AuthorityDto

fun Authority.toAuthorityDto(): AuthorityDto {
    return AuthorityDto(
        this.id!!,
        this.roleName!!,
        this.hierarchy!!,
    )
}
