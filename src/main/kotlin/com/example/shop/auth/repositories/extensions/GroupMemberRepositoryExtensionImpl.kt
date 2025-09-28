package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.domain.GroupMember
import com.example.shop.auth.domain.QAccount
import com.example.shop.auth.domain.QAccountGroup
import com.example.shop.auth.domain.QGroupMember
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class GroupMemberRepositoryExtensionImpl :
    QuerydslRepositorySupport(GroupMember::class.java),
    GroupMemberRepositoryExtension {

    val groupMember = QGroupMember.groupMember
    val accountGroup = QAccountGroup.accountGroup
    val account = QAccount.account

    override fun findAllByAccountGroupAndAccountId(accountGroupIds: List<Long>, accountIds: List<Long>): List<GroupMember> {
        return from(groupMember)
            .where(groupMember.account.id.`in`(accountIds))
            .where(groupMember.accountGroup.id.`in`(accountGroupIds))
            .fetch()
    }
}
