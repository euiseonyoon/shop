package com.example.shop.products.controllers

import com.example.shop.common.response.GlobalResponse
import com.example.shop.common.response.PagedResponse
import com.example.shop.constants.ROLE_ADMIN
import com.example.shop.products.domain.Product
import com.example.shop.products.models.CreateProductRequest
import com.example.shop.products.models.UpdateProductRequest
import com.example.shop.products.services.ProductService
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/product")
class ProductController(
    private val productService: ProductService
) {
    @PreAuthorize("hasRole('${ROLE_ADMIN}')")
    @PostMapping
    fun createProducts(
        @RequestBody(required = true) request: List<CreateProductRequest>
    ): GlobalResponse<List<Product>> {
        return productService.createMany(request).let {
            GlobalResponse.create(it)
        }
    }

    @GetMapping
    fun getProducts(
        @RequestParam(required = true) categoryId: Long,
        @RequestParam(required = true) includeChildren: Boolean,
        pageable: Pageable,
    ): GlobalResponse<PagedResponse<Product>> {
        return productService.findByCategoryId(categoryId, includeChildren, pageable).let {
            GlobalResponse.create(PagedResponse.fromPage(it))
        }
    }

    @PreAuthorize("hasRole('${ROLE_ADMIN}')")
    @PatchMapping
    fun updateMany(@RequestBody req: List<UpdateProductRequest>): GlobalResponse<List<Product>> {
        return productService.updateMany(req).let {
            GlobalResponse.create(it)
        }
    }
}
