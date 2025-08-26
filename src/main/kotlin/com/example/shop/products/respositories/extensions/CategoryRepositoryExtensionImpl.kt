package com.example.shop.products.respositories.extensions

import com.example.shop.products.domain.Category
import com.example.shop.products.domain.QCategory
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository


@Repository
class CategoryRepositoryExtensionImpl(
    private val queryFactory: JPAQueryFactory
) : QuerydslRepositorySupport(Category::class.java), CategoryRepositoryExtension {
    override fun searchWithIdsOrNames(names: List<String>?, ids: List<Long>?, pageable: Pageable): Page<Category> {
        val category = QCategory.category
        val builder = BooleanBuilder()

        // 1. names가 존재하면 OR 조건 추가
        if (names != null && names.isNotEmpty()) {
            builder.or(category.name.`in`(names))
        }

        // 2. ids가 존재하면 OR 조건 추가
        if (!ids.isNullOrEmpty()) {
            builder.or(category.id.`in`(ids))
        }

        val totalCount = queryFactory.select(category.count())
            .from(category)
            .where(builder)
            .fetchOne() ?: 0L

        val pagedQuery = queryFactory.selectFrom(category)
            .where(builder)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        val content = pagedQuery.fetch()

        return PageImpl(content, pageable, totalCount)
    }

    override fun searchByIdIncludeChildren(id: Long, includeChildren: Boolean): List<Category> {
        val category = QCategory.category

        if (includeChildren) {
            val parentSubQuery = QCategory("parentSubQuery")
            val parentCategoryName = JPAExpressions
                .select(parentSubQuery.name)
                .from(parentSubQuery)
                .where(parentSubQuery.id.eq(id))
                .limit(1)

            return from(category)
                .where(category.fullPath.contains(parentCategoryName))
                .fetch()
        } else {
            return from(category).where(category.id.eq(id)).fetch()
        }
    }
}
