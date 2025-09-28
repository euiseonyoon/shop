package com.example.shop.common.apis.models

import com.example.shop.auth.domain.Email
import org.springframework.data.domain.Pageable

data class AccountSearchCriteria(
    val accountIds: List<Long>?,
    val emails: List<Email>?,
    val enabled: Boolean?,
    val pageable: Pageable,
)
