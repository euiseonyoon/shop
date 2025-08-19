package com.example.shop.products.respositories.extensions

import com.example.shop.products.domain.Category
import com.example.shop.products.domain.QCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository


@Repository
class CategoryRepositoryExtensionImpl : QuerydslRepositorySupport(Category::class.java), CategoryRepositoryExtension {
    override fun searchWithIdsOrNames(names: List<String>?, ids: List<Long>?, pageable: Pageable): Page<Category> {
        val category = QCategory.category

        val query = from(category)
        if (names != null) {
            query.where(category.name.`in`(names))
        }
        if (ids != null) {
            query.where(category.id.`in`(ids))
        }
        val totalCount = query.fetchCount()
        val pagedQuery = getQuerydsl()!!.applyPagination(pageable, query)
        val content = pagedQuery.fetch()

        return PageImpl(content, pageable, totalCount)
    }
}
