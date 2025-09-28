package com.example.shop.auth.domain

import com.example.shop.common.utils.exceptions.AuthorityPrefixException
import com.example.shop.common.utils.exceptions.InvalidEmailAddressException
import com.example.shop.constants.EMAIL_REGEX
import com.example.shop.constants.ROLE_PREFIX
import jakarta.persistence.Embeddable
import java.util.regex.Pattern

@Embeddable
class Email(
    val address: String
) {

    init {
        if (!EMAIL_REGEX.matches(address)) {
            throw InvalidEmailAddressException(address)
        }
    }

    override fun toString(): String = address

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is Email) return false
        return other.address == this.address
    }

    override fun hashCode(): Int = address.hashCode()
}
