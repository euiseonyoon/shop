package com.example.shop.common.apis.models

import org.springframework.data.domain.Pageable

data class AccountSearchCriteria(
    val accountIds: List<Long>?,
    val emails: List<String>?,
    val enabled: Boolean?,
    val pageable: Pageable,
)
