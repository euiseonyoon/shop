package com.example.shop.products.models

import com.example.shop.common.apis.exceptions.BadRequestException

data class UpdateCategoryRequest(
    val id: Long,
    val name: String? = null,
    val changeToRootCategory: Boolean = false,
    val parentId: Long? = null,
    val isEnabled: Boolean? = null,
) {
    init {
        if (changeToRootCategory && parentId != null) {
            throw BadRequestException("루트 카테고리로 바꾸려면 parentId는 null 이어야함.")
        }
    }
}
