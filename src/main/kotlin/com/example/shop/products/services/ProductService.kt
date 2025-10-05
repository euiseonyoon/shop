package com.example.shop.products.services

import com.example.shop.common.apis.exceptions.BadRequestException
import com.example.shop.common.requireNonNegative
import com.example.shop.common.utils.JpaBatchHelper
import com.example.shop.products.domain.Category
import com.example.shop.products.domain.Product
import com.example.shop.products.models.CreateProductRequest
import com.example.shop.products.models.UpdateProductRequest
import com.example.shop.products.models.toEntity
import com.example.shop.products.respositories.CategoryRepository
import com.example.shop.products.respositories.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val categoryService: CategoryService,
    private val jpaBatchHelper: JpaBatchHelper,
) {
    @Transactional
    fun findByIdWithLock(productId: Long): Product? {
        return productRepository.findByIdWithLock(productId)
    }

    @Transactional(readOnly = true)
    fun findByIdsWithLock(productIds: List<Long>): List<Product> = productRepository.findAllByIdIn(productIds)

    @Transactional
    fun createMany(req: List<CreateProductRequest>): List<Product> {
        val categories = checkCategories(req.map { it.categoryId }.toSet())
        val entities = req.map { item ->
            item.toEntity(categories[item.categoryId]!!)
        }

        return jpaBatchHelper.batchInsert(entities)
    }

    private fun checkCategories(categoryIds: Set<Long>): Map<Long, Category> {
        val categories = categoryRepository.findAllById(categoryIds).associateBy { it.id }
        val missingIds = categoryIds - categories.keys
        if (missingIds.isNotEmpty()) {
            throw BadRequestException("존재하지 않는 카테고리 ID: $missingIds")
        }

        return categories
    }

    @Transactional(readOnly = true)
    fun findByCategoryId(categoryId: Long, includeChildren: Boolean, pageable: Pageable): Page<Product> {
        val categoryIds = categoryService.getByIdIncludeChildren(categoryId, includeChildren).map { it.id }
        return productRepository.findAllByCategoryIdIn(categoryIds, pageable)
    }

    @Transactional
    fun updateMany(req: List<UpdateProductRequest>): List<Product> {
        val categories = checkCategories(req.mapNotNull { it.categoryId }.toSet())
        val reqMap = req.associateBy { it.id }
        val entities = productRepository.findAllById(reqMap.keys)
        if (reqMap.keys.size != entities.size) {
            throw BadRequestException("주어진 정보중, 없는 Category가 있음.")
        }

        entities.forEach { entity ->
            val request = reqMap[entity.id]!!

            request.name?.let { entity.name = it }
            request.count?.let { entity.stock = it.requireNonNegative(BadRequestException("수량은 음수가 안됨.")) }
            request.price?.let { entity.price = it.requireNonNegative(BadRequestException("가격은 음수가 안됨.")) }
            request.categoryId?.let { entity.category = categories[it]!! }
        }

        return jpaBatchHelper.batchUpdate(entities)
    }

    @Transactional
    fun isEnabledProductExist(productId: Long): Boolean {
        return productRepository.existsByIdAndIsEnabledTrue(productId)
    }

}
