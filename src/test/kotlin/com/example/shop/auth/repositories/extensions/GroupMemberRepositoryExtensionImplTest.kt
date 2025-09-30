package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.common.TestAuthorityFactory
import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.Email
import com.example.shop.auth.domain.GroupMember
import com.example.shop.auth.domain.Role
import com.example.shop.auth.repositories.AccountGroupRepository
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.auth.repositories.GroupMemberRepository
import com.example.shop.constants.ROLE_USER
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class GroupMemberRepositoryExtensionImplTest(
    private val gmRepository: GroupMemberRepository,
    private val accountRepository: AccountRepository,
    private val agRepository: AccountGroupRepository,
    private val authorityFactory: TestAuthorityFactory,
    private val em: EntityManager,
) {
    lateinit var accountGroupFirst: AccountGroup
    lateinit var accountGroupSecond: AccountGroup
    lateinit var accountGroupWithNoAccount: AccountGroup

    lateinit var firstGroupMember: List<GroupMember>
    lateinit var secondGroupMember: List<GroupMember>

    lateinit var accounts: List<Account>

    @BeforeEach
    fun init() {
        val authority = authorityFactory.createAuthorities(em, listOf(Role(ROLE_USER) to 100)).first()
        accountGroupFirst = AccountGroup("group1").also { agRepository.save(it) }
        accountGroupSecond = AccountGroup("group2").also { agRepository.save(it) }
        accountGroupWithNoAccount = AccountGroup("group_no_account").also { agRepository.save(it) }

        accounts = listOf(
            Account(Email("first@gmail.com"), "first", authority),
            Account(Email("second@gmail.com"), "second", authority),
            Account(Email("third@gmail.com"), "third", authority),
        ).map {
            accountRepository.save(it)
            it
        }

        firstGroupMember = listOf(accounts[0], accounts[1]).map {
            GroupMember(accountGroupFirst, it).also { gmRepository.save(it) }
        }

        secondGroupMember = listOf(
            GroupMember(accountGroupSecond, accounts[2]).also { gmRepository.save(it) }
        )
    }

    @Test
    @Transactional
    fun `test findAllByAccountGroupAndAccountId accountIds`() {
        // GIVEN
        val allAccountGroups = listOf(accountGroupFirst, accountGroupSecond, accountGroupWithNoAccount)
        val accountGroupIds = allAccountGroups.map { it.id }
        val targetAccountIds = firstGroupMember.map { it.account.id }

        // WHEN
        val result = gmRepository.findAllByAccountGroupAndAccountId(accountGroupIds, targetAccountIds)

        // THEN
        assertEquals(firstGroupMember.toSet(), result.toSet())
    }

    @Test
    @Transactional
    fun `test findAllByAccountGroupAndAccountId accountGroupIds`() {
        // GIVEN
        val targetAccountGroupIds = listOf(accountGroupSecond.id)
        val allAccountIds = accounts.map { it.id }

        // WHEN
        val result = gmRepository.findAllByAccountGroupAndAccountId(targetAccountGroupIds, allAccountIds)

        // THEN
        assertEquals(secondGroupMember.toSet(), result.toSet())
    }

    @Test
    @Transactional
    fun `test findAllByAccountGroupAndAccountId complex`() {
        // GIVEN
        val targetAccountGroupIds = listOf(accountGroupFirst.id)
        val targetAccountIds = listOf(accounts[0], accounts[2]).map { it.id }

        // WHEN
        val result = gmRepository.findAllByAccountGroupAndAccountId(targetAccountGroupIds, targetAccountIds)

        // THEN
        assertEquals(1, result.size)
        assertEquals(firstGroupMember[0], result.first())
    }

    @Test
    @Transactional
    fun `test findAllByAccountGroupAndAccountId complex2`() {
        // GIVEN
        val targetAccountGroupIds = listOf(accountGroupWithNoAccount.id)
        val targetAccountIds = listOf(accounts[0], accounts[2]).map { it.id }

        // WHEN
        val result = gmRepository.findAllByAccountGroupAndAccountId(targetAccountGroupIds, targetAccountIds)

        // THEN
        assertTrue { result.isEmpty() }
    }
}
