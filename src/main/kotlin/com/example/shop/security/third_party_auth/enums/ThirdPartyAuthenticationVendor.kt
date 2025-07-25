package com.example.shop.security.third_party_auth.enums

enum class ThirdPartyAuthenticationVendor {
    GOOGLE,
    KAKAO,
    NAVER,
    ;

    companion object {
        fun fromString(vendorName: String): ThirdPartyAuthenticationVendor {
            return entries.firstOrNull { it.name.equals(vendorName, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown third-party vendor: $vendorName")
        }
    }
}
