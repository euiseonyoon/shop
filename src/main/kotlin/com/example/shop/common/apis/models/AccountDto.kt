package com.example.shop.common.apis.models

open class AccountDto(
    open val id: Long,
    open val email: String,
    open val enabled: Boolean,
    open val nickname: String?,
)
