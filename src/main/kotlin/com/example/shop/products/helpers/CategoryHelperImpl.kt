package com.example.shop.products.helpers

import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.products.CATEGORY_PATH_DELIMITER
import com.example.shop.products.domain.Category
import com.example.shop.products.models.CreateCategoryRequest
import com.example.shop.products.models.UpdateCategoryRequest
import com.example.shop.products.models.toCategoryEntity
import com.example.shop.products.respositories.CategoryRepository
import org.springframework.stereotype.Component

@Component
class CategoryHelperImpl(
    private val categoryRepository: CategoryRepository,
) : CategoryHelper {
    override fun makeCategoriesToInsert(
        requests: List<CreateCategoryRequest>,
        parent: Category?,
    ): List<Category> {
        val categoriesToInsert = mutableListOf<Category>()

        requests.map { req ->
            val newCategory = req.toCategoryEntity(parent ?: getParentCategory(req.parentId))
            categoriesToInsert.add(newCategory)

            // 재귀 호출로 자식들 생성
            val children = req.children?.let { makeCategoriesToInsert(it, newCategory) } ?: emptyList()

            categoriesToInsert.addAll(children)
        }

        return categoriesToInsert
    }

    fun getParentCategory(parentId: Long?): Category? {
        return parentId?.let {
            categoryRepository.findById(it).orElseThrow {
                BadRequestException("Category parent not found with given id. parentId={${parentId}}")
            }
        }
    }

    override fun modifyCategoriesToUpdate(
        targetCategories: List<Category>,
        requestMap: Map<Long, UpdateCategoryRequest>,
    ): List<Category> {
        val modifiedCategories = mutableListOf<Category>()

        targetCategories.forEach { category ->
            val req = requestMap[category.id] ?: return@forEach
            val affectedChildren = mutableListOf<Category>()

            val isCategoryModified = assignValueAsRequest(req, category, affectedChildren)

            if (isCategoryModified) {
                modifiedCategories.add(category)
            }
            modifiedCategories.addAll(affectedChildren)
        }

        return modifiedCategories
    }

    fun updateChildrenPaths(parent: Category, affectedChildren: MutableList<Category>): List<Category> {
        val children = categoryRepository.findAllByParent(parent)
        return children.map { child ->
            child.fullPath = parent.fullPath + CATEGORY_PATH_DELIMITER + child.name
            affectedChildren.add(child)
            updateChildrenPaths(child, affectedChildren)
            child
        }
    }

    fun assignValueAsRequest(
        req: UpdateCategoryRequest,
        category: Category,
        affectedChildren: MutableList<Category>,
    ): Boolean {
        var isCategoryModified = false

        if (updateParent(req, category)) {
            updateChildrenPaths(category, affectedChildren)
            isCategoryModified = true
        }
        req.name?.let {
            category.name = it
            isCategoryModified = true
        }
        req.isEnabled?.let {
            updateEnabled(category, it, affectedChildren)
            isCategoryModified = true
        }

        return isCategoryModified
    }

    fun updateEnabled(
        category: Category,
        enabled: Boolean,
        affectedChildren: MutableList<Category>,
    ): List<Category> {
        category.isEnabled = enabled
        return affectedChildren.map {
            it.isEnabled = enabled
            it
        }
    }

    fun updateParent(req: UpdateCategoryRequest, category: Category): Boolean {
        return when {
            req.changeToRootCategory && req.parentId != null -> {
                throw BadRequestException("루트 카테고리로 바꾸려면 parentId는 null 이어야함.")
            }

            req.changeToRootCategory -> {
                category.parent = null
                category.fullPath = "${category.name}"
                true
            }

            req.parentId != null -> {
                val newParent = categoryRepository.findById(req.parentId)
                    .orElseThrow { IllegalArgumentException("부모 카테고리(id=${req.parentId})가 존재하지 않습니다.") }

                // 사이클 방지
                if (isCycle(category.id, newParent)) {
                    throw IllegalArgumentException("자기 자신이나 자손을 부모로 지정할 수 없습니다.")
                }

                category.parent = newParent
                category.fullPath = newParent.fullPath + CATEGORY_PATH_DELIMITER + category.name
                true
            }

            else -> false
        }
    }

    // 사이클 방지: 자기 자신을 부모로 지정하거나 자식 중 하나를 부모로 지정하는 경우
    fun isCycle(selfId: Long, newParent: Category): Boolean {
        var cursor: Category? = newParent
        while (cursor != null) {
            if (cursor.id == selfId) return true
            cursor = cursor.parent
        }
        return false
    }
}
