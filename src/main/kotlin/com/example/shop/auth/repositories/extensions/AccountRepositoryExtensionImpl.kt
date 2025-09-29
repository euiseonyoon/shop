package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.Email
import com.example.shop.auth.domain.QAccount
import com.example.shop.common.apis.models.AccountSearchCriteria
import com.example.shop.common.utils.QuerydslPagingHelper
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Repository

@Repository
class AccountRepositoryExtensionImpl(
    private val queryFactory: JPAQueryFactory,
    private val querydslPagingHelper: QuerydslPagingHelper,
) : AccountRepositoryExtension {
    private val account = QAccount.account

    private fun accountIdsIn(accountIds: List<Long>?): BooleanExpression? {
        if (accountIds.isNullOrEmpty()) {
            return null
        }
        return account.id.`in`(accountIds)
    }

    private fun emailsIn(emails: List<Email>?): BooleanExpression? {
        if (emails.isNullOrEmpty()) {
            return null
        }
        return account.email.`in`(emails)
    }

    private fun eqEnabled(enabled: Boolean?): BooleanExpression? {
        if (enabled == null) {
            return null
        }
        return account.enabled.eq(enabled)
    }

    override fun findWithCriteria(criteria: AccountSearchCriteria): Page<Account> {
        val whereClauses = arrayOf(
            accountIdsIn(criteria.accountIds),
            emailsIn(criteria.emails),
            eqEnabled(criteria.enabled)
        ).filterNotNull().toTypedArray()

        val baseQuery = queryFactory.selectFrom(account)

        val totalCount = querydslPagingHelper.getTotalCount(account, whereClauses)
        val content = querydslPagingHelper.getContent(baseQuery, whereClauses, criteria.pageable)

        return PageImpl(content, criteria.pageable, totalCount)
    }
}
