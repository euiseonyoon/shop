package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.QAccount
import com.example.shop.auth.domain.QAuthority
import com.example.shop.auth.domain.QAccountGroup
import com.example.shop.auth.domain.QGroupAuthority
import com.example.shop.auth.domain.QGroupMember
import com.example.shop.common.apis.models.AccountSearchCriteria
import com.querydsl.jpa.JPQLQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class AccountRepositoryExtensionImpl : QuerydslRepositorySupport(Account::class.java), AccountRepositoryExtension {
    val account = QAccount.account
    val authority = QAuthority.authority

    override fun findWithCriteria(criteria: AccountSearchCriteria): Page<Account> {
        val query = from(account)

        if (criteria.accountIds != null) {
            query.where(account.id.`in`(criteria.accountIds))
        }
        if (criteria.emails != null) {
            query.where(account.email.`in`(criteria.emails))
        }
        if (criteria.enabled != null) {
            query.where(account.enabled.eq(criteria.enabled))
        }

        val totalCount = query.fetchCount()
        val pagedQuery = getQuerydsl()!!.applyPagination(criteria.pageable, query)
        val content = pagedQuery.fetch()

        return PageImpl(content, criteria.pageable, totalCount)
    }
}
