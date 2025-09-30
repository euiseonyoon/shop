package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.common.TestAuthorityFactory
import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.Email
import com.example.shop.auth.domain.Role
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.common.apis.models.AccountSearchCriteria
import com.example.shop.constants.ROLE_USER
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
class AccountRepositoryExtensionImplTest(
    private val em: EntityManager,
    private val accountRepository: AccountRepository,
    private val authorityFactory: TestAuthorityFactory,
) {
    lateinit var accounts: List<Account>
    lateinit var accountsIdMap: Map<Long, Account>

    @BeforeEach
    fun init() {
        val authority = authorityFactory.createAuthorities(em, listOf(Role(ROLE_USER) to 100)).first()

        accounts = listOf(
            Account(Email("first@gmail.com"), "first", authority, true),
            Account(Email("second@gmail.com"), "second", authority, true),
            Account(Email("thrid@gmail.com"), "third", authority, true),
            Account(Email("fourth@gmail.com"), "fourth", authority, false),
        ).map {
            accountRepository.save(it)
            it
        }
        accountsIdMap = accounts.associateBy { it.id }
    }

    @Test
    @Transactional
    fun `test findWithCriteria id`() {
        // GIVEN
        val targetId = accounts.first().id
        val criteria = AccountSearchCriteria(
            accountIds = listOf(targetId),
            emails = null,
            enabled = null,
            pageable = PageRequest.of(0, 2)
        )
        // WHEN
        val result = accountRepository.findWithCriteria(criteria)

        // THEN
        assertEquals(1, result.content.size)
        assertEquals(1, result.totalElements.toInt())
        assertEquals(accountsIdMap[targetId], result.content.first())
    }

    @Test
    @Transactional
    fun `test findWithCriteria email`() {
        // GIVEN
        val targetEmails = listOf(accounts[0].email, accounts[1].email, accounts[2].email)
        val criteria = AccountSearchCriteria(
            accountIds = null,
            emails = targetEmails,
            enabled = null,
            pageable = PageRequest.of(0, 2)
        )
        // WHEN
        val result = accountRepository.findWithCriteria(criteria)

        // THEN
        assertEquals(accounts[0], result.content[0])
        assertEquals(accounts[1], result.content[1])

        assertEquals(2, result.content.size)
        assertEquals(targetEmails.size, result.totalElements.toInt())
    }

    @Test
    @Transactional
    fun `test findWithCriteria enabled`() {
        // GIVEN
        val criteria = AccountSearchCriteria(
            accountIds = null,
            emails = null,
            enabled = false,
            pageable = PageRequest.of(0, 2)
        )
        // WHEN
        val result = accountRepository.findWithCriteria(criteria)

        // THEN
        assertEquals(accounts[3], result.content[0])
        assertEquals(1, result.content.size)
        assertEquals(1, result.totalElements.toInt())
    }

    @Test
    @Transactional
    fun `test findWithCriteria complex`() {
        // GIVEN
        val targetAccount = accounts[2]
        val criteria = AccountSearchCriteria(
            accountIds = listOf(accounts.first().id, targetAccount.id),
            emails = listOf(targetAccount.email),
            enabled = true,
            pageable = PageRequest.of(0, 2)
        )
        // WHEN
        val result = accountRepository.findWithCriteria(criteria)

        // THEN
        assertEquals(targetAccount, result.content[0])
        assertEquals(1, result.content.size)
        assertEquals(1, result.totalElements.toInt())
    }
}
