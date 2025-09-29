package com.example.shop.common.utils

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.jpa.impl.JPAQuery
import org.springframework.data.domain.Pageable


interface QuerydslPagingHelper {
    fun <T> getTotalCount(
        qClass: EntityPathBase<T>,
        whereClauses: Array<BooleanExpression>,
    ): Long

    fun <T> getContent(
        baseQuery: JPAQuery<T>,
        whereClauses: Array<BooleanExpression>,
        pageable: Pageable,
    ): List<T>
}
