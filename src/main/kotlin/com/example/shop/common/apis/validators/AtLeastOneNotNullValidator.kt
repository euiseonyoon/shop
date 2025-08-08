package com.example.shop.common.apis.validators

import com.example.shop.admin.models.auth.GroupAuthorityUpdateRequest
import com.example.shop.common.apis.annotations.AtLeastOneNotNull
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class AtLeastOneNotNullValidator : ConstraintValidator<AtLeastOneNotNull, Any> {
    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        return when(value) {
            is GroupAuthorityUpdateRequest -> {
                value.name != null || value.groupName != null
            }
            else -> true
        }
    }
}
