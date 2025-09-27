package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.auth.domain.QAccount
import com.example.shop.auth.domain.QAccountGroup
import com.example.shop.auth.domain.QGroupAuthority
import com.example.shop.auth.domain.QGroupMember
import com.example.shop.auth.models.AccountGroupAuthorityDto
import com.example.shop.auth.models.QAccountGroupAuthorityDto
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class GroupAuthorityRepositoryExtensionImpl :
    GroupAuthorityRepositoryExtension, QuerydslRepositorySupport(GroupAuthority::class.java) {

    val groupAuthority = QGroupAuthority.groupAuthority
    val groupMember = QGroupMember.groupMember
    val accountGroup = QAccountGroup.accountGroup
    val account = QAccount.account

    override fun getAccountGroupAuthorityDtos(accountIds: List<Long>): List<AccountGroupAuthorityDto> {
        val results = from(groupAuthority)
            .select(
                QAccountGroupAuthorityDto(account, groupAuthority)
            )
            .leftJoin(accountGroup, groupAuthority.accountGroup).fetchJoin()
            .leftJoin(groupMember.accountGroup, accountGroup)
            .leftJoin(groupMember.account, account)
            .where(account.id.`in`(accountIds))
            .fetch()

        return results
    }

    override fun getAccountGroupAuthorities(accountId: Long): List<GroupAuthority> {
        return from(groupAuthority)
            .distinct()
            .leftJoin(accountGroup, groupAuthority.accountGroup).fetchJoin()
            .leftJoin(groupMember.accountGroup, accountGroup)
            .leftJoin(groupMember.account, account)
            .where(account.id.eq(accountId))
            .fetch()
    }

    override fun findAllByAccountGroupIdIn(accountGroupIds: List<Long>): List<GroupAuthority> {
        return from(groupAuthority)
            .leftJoin(accountGroup, groupAuthority.accountGroup).fetchJoin()
            .where(accountGroup.id.`in`(accountGroupIds))
            .fetch()
    }
}
