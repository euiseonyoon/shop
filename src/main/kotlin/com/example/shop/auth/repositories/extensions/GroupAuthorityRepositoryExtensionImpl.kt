package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.auth.domain.QAccount
import com.example.shop.auth.domain.QAccountGroup
import com.example.shop.auth.domain.QGroupAuthority
import com.example.shop.auth.domain.QGroupMember
import com.example.shop.auth.models.AccountGroupAuthorityDto
import com.querydsl.core.types.Projections
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
                Projections.constructor(AccountGroupAuthorityDto::class.java, account, groupAuthority)
            )
            .innerJoin(accountGroup).on(groupAuthority.accountGroup.eq(accountGroup))
            .innerJoin(groupMember).on(accountGroup.eq(groupMember.accountGroup))
            .innerJoin(account).on(groupMember.account.eq(account))
            .where(account.id.`in`(accountIds))
            .fetch()

        return results
    }

    override fun getAccountGroupAuthorities(accountId: Long): List<GroupAuthority> {
        return queryFactory.selectFrom(groupAuthority)
            .innerJoin(accountGroup).on(groupAuthority.accountGroup.eq(accountGroup))
            .innerJoin(groupMember).on(groupMember.accountGroup.eq(accountGroup))
            .innerJoin(account).on(groupMember.account.eq(account))
            .where(account.id.eq(accountId))
            .fetch()
    }

    override fun findAllByAccountGroupIdIn(accountGroupIds: List<Long>): List<GroupAuthority> {
        return queryFactory.selectFrom(groupAuthority)
            .innerJoin(accountGroup).on(groupAuthority.accountGroup.eq(accountGroup))
            .fetch()
    }
}
