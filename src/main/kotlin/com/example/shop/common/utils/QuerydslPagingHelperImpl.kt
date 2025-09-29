package com.example.shop.common.utils

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class QuerydslPagingHelperImpl(
    private val queryFactory: JPAQueryFactory
) : QuerydslPagingHelper {
    override fun <T> getTotalCount(
        qClass: EntityPathBase<T>,
        whereClauses: Array<BooleanExpression>,
    ): Long {
        return queryFactory
            .select(qClass.count())
            .from(qClass)
            .where(*whereClauses)
            .fetchOne() ?: 0L
    }

    override fun <T> getContent(
        baseQuery: JPAQuery<T>,
        whereClauses: Array<BooleanExpression>,
        pageable: Pageable,
    ): List<T> {
        return baseQuery.where(*whereClauses)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
    }
}
