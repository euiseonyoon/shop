package com.example.shop.common.response

import kotlinx.serialization.Serializable
import org.springframework.data.domain.Page

@Serializable
data class PagedResponse<T>(
    val result: List<T>,
    val page: Int,
    val perPage: Int,
    val totalCount: Long,
) {
    companion object {
        fun <T> fromPage(page: Page<T>): PagedResponse<T> {
            return PagedResponse(
                result = page.content.toList(),
                page = page.pageable.pageNumber, // 페이지 번호는 0부터 시작
                perPage = page.pageable.pageSize,
                totalCount = page.totalElements,
            )
        }
    }
}
