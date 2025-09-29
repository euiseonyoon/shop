package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.auth.domain.QAccount
import com.example.shop.auth.domain.QAccountGroup
import com.example.shop.auth.domain.QGroupAuthority
import com.example.shop.auth.domain.QGroupMember
import com.example.shop.auth.models.AccountGroupAuthorityDto
import com.example.shop.auth.models.QAccountGroupAuthorityDto
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class GroupAuthorityRepositoryExtensionImpl(
    private val queryFactory: JPAQueryFactory,
) : GroupAuthorityRepositoryExtension {

    private val groupAuthority = QGroupAuthority.groupAuthority
    private val groupMember = QGroupMember.groupMember
    private val accountGroup = QAccountGroup.accountGroup
    private val account = QAccount.account

    override fun getAccountGroupAuthorityDtos(accountIds: List<Long>): List<AccountGroupAuthorityDto> {
        val results = queryFactory.selectFrom(groupAuthority)
            .select(
                QAccountGroupAuthorityDto(account, groupAuthority)
            )
            .leftJoin(accountGroup).on(groupAuthority.accountGroup.eq(accountGroup))
            .leftJoin(groupMember.accountGroup, accountGroup)
            .leftJoin(groupMember.account, account)
            .where(account.id.`in`(accountIds))
            .fetch()

        return results
    }

    override fun getAccountGroupAuthorities(accountId: Long): List<GroupAuthority> {
        return queryFactory.selectFrom(groupAuthority)
            .distinct()
            .leftJoin(accountGroup).on(groupAuthority.accountGroup.eq(accountGroup))
            .leftJoin(groupMember).on(groupMember.accountGroup.eq(accountGroup))
            .leftJoin(account).on(account.eq(groupMember.account))
            .where(account.id.eq(accountId))
            .fetch()
    }

    override fun findAllByAccountGroupIdIn(accountGroupIds: List<Long>): List<GroupAuthority> {
        return queryFactory.selectFrom(groupAuthority)
            .leftJoin(accountGroup).on(groupAuthority.accountGroup.eq(accountGroup))
            .fetch()
    }
}
