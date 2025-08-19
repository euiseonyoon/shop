package com.example.shop.products.services

import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.common.utils.JpaBatchHelper
import com.example.shop.products.CATEGORY_PATH_DELIMITER
import com.example.shop.products.domain.Category
import com.example.shop.products.models.CreateCategoryRequest
import com.example.shop.products.models.UpdateCategoryRequest
import com.example.shop.products.models.toCategoryEntity
import com.example.shop.products.respositories.CategoryRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val jpaBatchHelper: JpaBatchHelper,
) {
    @Transactional(readOnly = true)
    fun getMany(names: List<String>?, ids: List<Long>?, pageable: Pageable): Page<Category> {
        return categoryRepository.searchWithIdsOrNames(names, ids, pageable)
    }

    @Transactional
    fun createMany(requests: List<CreateCategoryRequest>): List<Category> {
        val entities = createEntityForCreate(requests, null)
        val createdEntities = jpaBatchHelper.batchInsert(entities)
        return createdEntities
    }

    fun createEntityForCreate(
        requests: List<CreateCategoryRequest>,
        _parent: Category?,
    ): List<Category> {
        val result = mutableListOf<Category>()

        requests.map { req ->
            val parent = if (_parent != null) {
                _parent
            } else {
                req.parentId?.let {
                    categoryRepository.findById(it).orElseThrow(
                        Supplier {
                            BadRequestException("Category parent not found with given id. parentId={${req.parentId}}")
                        }
                    )
                }
            }
            val categoryEntity = req.toCategoryEntity(parent)
            result.add(categoryEntity)

            // 재귀 호출로 자식들 생성
            val children = req.children?.let {
                createEntityForCreate(it, categoryEntity)
            } ?: emptyList()

            result.addAll(children)
        }

        return result
    }

    @Transactional
    fun updateMany(requests: List<UpdateCategoryRequest>): List<Category> {
        val requestMap = requests.associateBy { it.id }
        val entities = categoryRepository.findAllById(requestMap.keys)

        val updatedEntities = mutableListOf<Category>()

        for (entity in entities) {
            val req = requestMap[entity.id] ?: continue
            val affectedChildren = mutableListOf<Category>()

            req.name?.let { entity.name = it }
            val isParentChanged = updateParent(req, entity)
            updatedEntities.add(entity)

            if (isParentChanged) {
                updateChildrenPaths(entity, affectedChildren)
            }

            req.isEnabled?.let {
                changeEnabled(entity, it, affectedChildren)
            }
            updatedEntities.addAll(affectedChildren)
        }

        // isLast 계산: 자식이 없으면 true
        updatedEntities.forEach { entity ->
            val hasChildren = categoryRepository.findAllByParent(entity).isNotEmpty()
            entity.isLast = !hasChildren
        }

        return jpaBatchHelper.batchUpdate(updatedEntities)
    }

    fun updateChildrenPaths(parent: Category, affectedChildren: MutableList<Category>) {
        val children = categoryRepository.findAllByParent(parent)
        children.forEach { child ->
            child.fullPath = parent.fullPath + CATEGORY_PATH_DELIMITER + child.name
            affectedChildren.add(child)
            updateChildrenPaths(child, affectedChildren)
        }
    }

    fun changeEnabled(
        entity: Category,
        enabled: Boolean,
        affectedChildren: MutableList<Category>,
    ) {
        entity.isEnabled = enabled
        affectedChildren.forEach { child ->
            child.isEnabled = enabled
        }
    }

    fun updateParent(req: UpdateCategoryRequest, entity: Category): Boolean {
        return when {
            req.changeToRootCategory && req.parentId != null -> {
                throw BadRequestException("루트 카테고리로 바꾸려면 parentId는 null 이어야함.")
            }

            req.changeToRootCategory -> {
                entity.parent = null
                entity.fullPath = "${entity.name}"
                true
            }

            req.parentId != null -> {
                val newParent = categoryRepository.findById(req.parentId)
                    .orElseThrow { IllegalArgumentException("부모 카테고리(id=${req.parentId})가 존재하지 않습니다.") }

                // 사이클 방지
                if (isCycle(entity.id!!, newParent)) {
                    throw IllegalArgumentException("자기 자신이나 자손을 부모로 지정할 수 없습니다.")
                }

                entity.parent = newParent
                entity.fullPath = newParent.fullPath + CATEGORY_PATH_DELIMITER + entity.name
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
