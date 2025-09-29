package com.example.shop.products.services

import com.example.shop.common.utils.JpaBatchHelper
import com.example.shop.products.domain.Category
import com.example.shop.products.helpers.CategoryHelper
import com.example.shop.products.models.CreateCategoryRequest
import com.example.shop.products.models.UpdateCategoryRequest
import com.example.shop.products.respositories.CategoryRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val jpaBatchHelper: JpaBatchHelper,
    private val categoryHelper: CategoryHelper,
) {
    @Transactional(readOnly = true)
    fun getMany(names: List<String>?, ids: List<Long>?, pageable: Pageable): Page<Category> {
        return categoryRepository.searchWithIdsOrNames(names, ids, pageable)
    }

    @Transactional
    fun createMany(requests: List<CreateCategoryRequest>): List<Category> {
        val categoriesToInsert = categoryHelper.makeCategoriesToInsert(requests, null)

        return jpaBatchHelper.batchInsert(categoriesToInsert)
    }

    @Transactional
    fun updateMany(requests: List<UpdateCategoryRequest>): List<Category> {
        val requestMap = requests.associateBy { it.id }

        val modifiedCategories = categoryHelper.modifyCategoriesToUpdate(
            categoryRepository.findAllById(requestMap.keys),
            requestMap,
        ).map {
            it.isLast = !hasChildren(it)
            it
        }

        return jpaBatchHelper.batchUpdate(modifiedCategories)
    }

    private fun hasChildren(category: Category): Boolean {
        return categoryRepository.existsByParent(category)
    }

    @Transactional(readOnly = true)
    fun getByIdIncludeChildren(categoryId: Long, includeChildren: Boolean): List<Category> {
        return categoryRepository.searchByIdIncludeChildren(categoryId, includeChildren)
    }
}
