package com.example.shop.products.controllers

import com.example.shop.common.response.GlobalResponse
import com.example.shop.constants.ROLE_ADMIN
import com.example.shop.products.domain.Category
import com.example.shop.products.models.CreateCategoryRequest
import com.example.shop.products.services.CategoryService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/category")
class CategoryController(
    private val categoryService: CategoryService,
) {
    @PreAuthorize("hasRole('${ROLE_ADMIN}')")
    @PostMapping
    fun createCategories(
        @RequestBody req: List<CreateCategoryRequest>
    ): GlobalResponse<List<Category>> {
        return categoryService.createMany(req).let {
            GlobalResponse.create(it)
        }
    }
}
