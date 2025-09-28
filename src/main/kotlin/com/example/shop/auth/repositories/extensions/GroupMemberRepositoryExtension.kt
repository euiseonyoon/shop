package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.domain.GroupMember

interface GroupMemberRepositoryExtension {
    fun findAllByAccountGroupAndAccountId(accountGroupIds: List<Long>, accountIds: List<Long>): List<GroupMember>
}
