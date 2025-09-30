package com.example.shop.auth.repositories.extensions

import com.example.shop.auth.common.TestAuthorityFactory
import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.Email
import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.auth.domain.GroupMember
import com.example.shop.auth.domain.Role
import com.example.shop.auth.repositories.AccountGroupRepository
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.auth.repositories.GroupAuthorityRepository
import com.example.shop.auth.repositories.GroupMemberRepository
import com.example.shop.constants.ROLE_PREFIX
import com.example.shop.constants.ROLE_USER
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class GroupAuthorityRepositoryExtensionImplTest(
    private val em: EntityManager,
    private val authorityFactory: TestAuthorityFactory,
    private val gmRepository: GroupMemberRepository,
    private val accountRepository: AccountRepository,
    private val agRepository: AccountGroupRepository,
    private val gaRepository: GroupAuthorityRepository
) {
    lateinit var accounts: List<Account>

    lateinit var firstAccountGroup: AccountGroup
    lateinit var secondAccountGroup: AccountGroup

    lateinit var firstAccountGroupAuthorities: List<GroupAuthority>
    lateinit var secondAccountGroupAuthorities: List<GroupAuthority>

    @BeforeEach
    fun init() {
        // Set `Authority`
        val authority = authorityFactory.createAuthorities(em, listOf(Role(ROLE_USER) to 100)).first()

        // Set `AccountGroup`
        firstAccountGroup = AccountGroup("group1").also { agRepository.save(it) }
        secondAccountGroup = AccountGroup("group2").also { agRepository.save(it) }

        // Set `Account`
        accounts = listOf(
            Account(Email("first@gmail.com"), "first", authority, true),
            Account(Email("second@gmail.com"), "second", authority, true),
            Account(Email("thrid@gmail.com"), "third", authority, true),
            Account(Email("fourth@gmail.com"), "fourth", authority, false),
        ).map {
            accountRepository.save(it)
            it
        }

        // Set `GroupMember`
        listOf(accounts[0], accounts[1]).map {
            GroupMember(firstAccountGroup, it).also { gmRepository.save(it) }
        }
        GroupMember(secondAccountGroup, accounts[2]).also { gmRepository.save(it) }

        // Set `GroupAuthority`
        firstAccountGroupAuthorities = listOf(
            GroupAuthority(Role("${ROLE_PREFIX}FirstGroup-1`"), firstAccountGroup),
            GroupAuthority(Role("${ROLE_PREFIX}FirstGroup-2`"), firstAccountGroup),
        ).map {
            gaRepository.save(it)
            it
        }
        secondAccountGroupAuthorities = listOf(
            GroupAuthority(Role("${ROLE_PREFIX}SecondGroup-1`"), secondAccountGroup),
        ).map {
            gaRepository.save(it)
            it
        }
    }

    @Test
    @Transactional
    fun `test getAccountGroupAuthorityDtos 1`() {
        // GIVEN: accounts[0] - firstAccountGroup 소속, accounts[2] - secondAccountGroup 소속
        val targetAccounts = listOf(accounts[0], accounts[2])
        val targetAccountIds = targetAccounts.map { it.id }

        // WHEN
        val results = gaRepository.getAccountGroupAuthorityDtos(targetAccountIds)

        // THEN
        val groupedResults = results.groupBy { it.account }
        assertEquals(2, groupedResults.size)
        targetAccounts.forEach {
            assertNotNull(groupedResults[it])
        }
        assertEquals(
            groupedResults[accounts[0]]?.map { it.groupAuthority }?.toSet(),
            firstAccountGroupAuthorities.toSet()
        )
        assertEquals(
            groupedResults[accounts[2]]?.map { it.groupAuthority }?.toSet(),
            secondAccountGroupAuthorities.toSet()
        )
    }

    @Test
    @Transactional
    fun `test getAccountGroupAuthorityDtos 2`() {
        // GIVEN: accounts[3] - 어떠한 `AccountGroup`에도 소속되지 않음
        val targetAccounts = listOf(accounts[3])
        val targetAccountIds = targetAccounts.map { it.id }

        // WHEN
        val results = gaRepository.getAccountGroupAuthorityDtos(targetAccountIds)

        // THEN
        assertTrue { results.isEmpty() }
    }

    @Test
    @Transactional
    fun `test getAccountGroupAuthorities 1`() {
        // GIVEN: account[1] - firstAccountGroup 소속
        val targetAccountId = accounts[1].id

        // WHEN
        val results = gaRepository.getAccountGroupAuthorities(targetAccountId)

        // THEN
        assertEquals(firstAccountGroupAuthorities.toSet(), results.toSet())
    }

    @Test
    @Transactional
    fun `test getAccountGroupAuthorities 2`() {
        // GIVEN: accounts[3] - 어떠한 `AccountGroup`에도 소속되지 않음
        val targetAccountId = accounts[3].id

        // WHEN
        val results = gaRepository.getAccountGroupAuthorities(targetAccountId)

        // THEN
        assertTrue { results.isEmpty() }
    }

    @Test
    @Transactional
    fun `test findAllByAccountGroupIdIn 1`() {
        // GIVEN
        val accountGroupIds = listOf(firstAccountGroup.id)

        // WHEN
        val results = gaRepository.findAllByAccountGroupIdIn(accountGroupIds)

        // THEN
        assertEquals(firstAccountGroupAuthorities.toSet(), results.toSet())
    }

    @Test
    @Transactional
    fun `test findAllByAccountGroupIdIn 2`() {
        // GIVEN
        val accountGroupIds = listOf(firstAccountGroup.id, secondAccountGroup.id)

        // WHEN
        val results = gaRepository.findAllByAccountGroupIdIn(accountGroupIds)

        // THEN
        assertEquals(
            (firstAccountGroupAuthorities + secondAccountGroupAuthorities).toSet(),
            results.toSet(),
        )
    }
}
