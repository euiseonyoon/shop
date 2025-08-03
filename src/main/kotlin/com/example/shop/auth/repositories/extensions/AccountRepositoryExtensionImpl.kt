package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.QAccount
import com.example.shop.auth.domain.QAuthority
import com.example.shop.auth.domain.QAccountGroup
import com.example.shop.auth.domain.QGroupAuthority
import com.example.shop.auth.domain.QGroupMember
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class AccountRepositoryExtensionImpl : QuerydslRepositorySupport(Account::class.java), AccountRepositoryExtension {
    override fun findWithAuthoritiesByEmail(email: String): Account? {
        val account = QAccount.account
        val authority = QAuthority.authority
        val groupMember = QGroupMember.groupMember
        val accountGroup = QAccountGroup.accountGroup
        val groupAuthority = QGroupAuthority.groupAuthority

        return from(account)
            .distinct()
            .leftJoin(account.authority, authority).fetchJoin()
            .leftJoin(account.groupMemberMap, groupMember).fetchJoin()
            .leftJoin(groupMember.accountGroup, accountGroup).fetchJoin()
            .leftJoin(accountGroup.authorities, groupAuthority).fetchJoin()
            .where(account.email.eq(email))
            .fetchOne()
    }
}
