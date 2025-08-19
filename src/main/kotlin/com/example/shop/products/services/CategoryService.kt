package com.example.shop.products.services

import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.common.utils.JpaBatchHelper
import com.example.shop.products.domain.Category
import com.example.shop.products.models.CreateCategoryRequest
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
}
