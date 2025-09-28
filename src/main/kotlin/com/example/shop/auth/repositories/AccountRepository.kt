package com.example.shop.auth.repositories

import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.Email
import com.example.shop.auth.repositories.extensions.AccountRepositoryExtension
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<Account, Long>, AccountRepositoryExtension {
    fun findByEmail(email: Email): Account?
}
