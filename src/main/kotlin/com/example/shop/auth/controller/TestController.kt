package com.example.shop.auth.controller

import com.example.shop.auth.ADMIN_NAME
import com.example.shop.auth.USER_NAME
import com.example.shop.auth.domain.Account
import com.example.shop.auth.models.AccountAuthenticationToken
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/test")
    fun test(): String {
        val authentication = SecurityContextHolder.getContext().authentication as AccountAuthenticationToken
        val account: Account = authentication.principal as Account
        return "hello ${account.email}"
    }

    @GetMapping("/test2")
    fun test2(
        @AuthenticationPrincipal
        account: Account
    ): String {
        val authorities = account.getGroupAuthorities()
        return "hello2 ${account.email}"
    }

    // @EnableMethodSecurity 를 사용해야 한다. SecurityConfig에 적용한다.
    @PreAuthorize("hasRole('${USER_NAME}')")
    @GetMapping("/test3")
    fun test3(): String {
        return "hello3"
    }

    @PreAuthorize("hasRole('${ADMIN_NAME}')")
    @GetMapping("/test4")
    fun test4(): String {
        return "hello4"
    }

    @PreAuthorize("hasRole('${USER_NAME}') and #userId == authentication.principal.id")
    @GetMapping("/test5")
    fun test5(userId: Long?): String {
        return "hello5"
    }
}
