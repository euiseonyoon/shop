package com.example.shop.common.apis.annotations

import com.example.shop.common.apis.validators.AtLeastOneNotNullValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AtLeastOneNotNullValidator::class])
annotation class AtLeastOneNotNull(
    val message: String = "둘 중 하나는 반드시 존재해야 합니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
