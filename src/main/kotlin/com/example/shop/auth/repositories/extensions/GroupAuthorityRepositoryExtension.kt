package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.auth.models.AccountGroupAuthorityDto

interface GroupAuthorityRepositoryExtension {
    fun getAccountGroupAuthorityDtos(accountIds: List<Long>): List<AccountGroupAuthorityDto>

    fun getAccountGroupAuthorities(accountId: Long): List<GroupAuthority>

    fun findAllByAccountGroupIdIn(accountGroupIds: List<Long>): List<GroupAuthority>
}
