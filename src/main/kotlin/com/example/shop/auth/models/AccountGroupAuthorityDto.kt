package com.example.shop.auth.models

import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.GroupAuthority
import com.querydsl.core.annotations.QueryProjection

data class AccountGroupAuthorityDto @QueryProjection constructor(
    val account: Account,
    val groupAuthority: GroupAuthority
)
