package com.example.shop.auth.services.facades

import com.example.shop.constants.ROLE_ADMIN
import com.example.shop.constants.ROLE_USER
import com.example.shop.auth.common.TestAccountGroupFactory
import com.example.shop.auth.common.TestAuthorityFactory
import com.example.shop.auth.common.TestGroupAuthorityFactory
import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.Email
import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.auth.domain.Role
import com.example.shop.auth.exceptions.AccountGroupPartiallyNotFoundException
import com.example.shop.constants.ADMIN_HIERARCHY
import com.example.shop.constants.DEFAULT_USER_HIERARCHY
import com.example.shop.constants.ROLE_PREFIX
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.springframework.security.test.context.support.WithMockUser

@SpringBootTest
class FacadeAccountCrudServiceTest(
    @PersistenceContext
    private val em: EntityManager,
    private val testAuthorityFactory: TestAuthorityFactory,
    private val testAccountGroupFactory: TestAccountGroupFactory,
    private val testGroupAuthorityFactory: TestGroupAuthorityFactory,
    private val facadeAccountCrudService: FacadeAccountCrudService,
) {
    private val groupNames = listOf("group1", "group2")
    private var groups: List<AccountGroup> = listOf()
    private var firstGroupAuthorities: List<GroupAuthority> = listOf()
    private var secondGroupAuthorities: List<GroupAuthority> = listOf()

    @BeforeEach
    fun init() {
        this.groups = testAccountGroupFactory.createAccountGroup(em, groupNames)

        this.firstGroupAuthorities = testGroupAuthorityFactory.createGroupAuthorities(
            em,
            listOf(Role("${ROLE_PREFIX}authority_1-1"), Role("${ROLE_PREFIX}authority_1-2")),
            groups.first()
        )
        this.secondGroupAuthorities = testGroupAuthorityFactory.createGroupAuthorities(
            em,
            listOf(Role("${ROLE_PREFIX}authority_2")),
            groups.last()
        )
    }

    @Test
    @Transactional
    fun `test user creation success`() {
        // GIVEN
        val authority = testAuthorityFactory.createAuthorities(em, listOf(Role(ROLE_USER) to DEFAULT_USER_HIERARCHY)).first()
        val email = Email("test@gmail.com")
        val rawPassword = "test"
        val nickname = "RyanAtBurst"

        // WHEN
        val newAccountDomain = facadeAccountCrudService.createUserAccount(
            email = email,
            rawPassword = rawPassword,
            nickname = nickname,
            thirdPartyOauthVendor = null,
            groupIds = groups.map { it.id }.toSet()
        )
        em.clear()

        // THEN
        assertEquals(email, newAccountDomain.account.email)
        assertEquals(nickname, newAccountDomain.account.nickname)
        assertEquals(Role(ROLE_USER), newAccountDomain.authority.role)
        val authorityGroupsIn = firstGroupAuthorities + secondGroupAuthorities
        assertEquals(authorityGroupsIn.toSet(), newAccountDomain.accountGroupMap.values.flatten().toSet())
    }

    @Test
    @Transactional
    fun `test user creation fail`() {
        // GIVEN
        val authority = testAuthorityFactory.createAuthorities(em, listOf(Role(ROLE_ADMIN) to ADMIN_HIERARCHY)).first()
        val email = Email("test@gmail.com")
        val rawPassword = "test"
        val nickname = "RyanAtBurst"

        // WHEN
        facadeAccountCrudService.createUserAccount(
            email = email,
            rawPassword = rawPassword,
            nickname = nickname,
            thirdPartyOauthVendor = null,
            groupIds = groups.map { it.id }.toSet()
        )
        em.clear()

        // THEN
        val accounts = em.createQuery("SELECT a FROM Account a WHERE a.email = :email", Account::class.java)
            .setParameter("email", email).resultList
        assertTrue(accounts.isEmpty())
    }

    @Test
    @Transactional
    @WithMockUser(username = "hello@gmail.com", roles = ["SUPER_ADMIN"])
    fun `test admin creation success`() {
        // GIVEN
        val authority = testAuthorityFactory.createAuthorities(em, listOf(Role(ROLE_ADMIN) to ADMIN_HIERARCHY)).first()
        val email = Email("test@gmail.com")
        val rawPassword = "test"
        val nickname = "RyanAtBurst"

        // WHEN
        val newAccountDomain = facadeAccountCrudService.createAdminAccount(
            email = email,
            rawPassword = rawPassword,
            nickname = nickname,
            groupIds = groups.map { it.id }.toSet()
        )
        em.clear()

        // THEN
        assertEquals(email, newAccountDomain.account.email)
        assertEquals(nickname, newAccountDomain.account.nickname)
        assertEquals(Role(ROLE_ADMIN), newAccountDomain.account.authority.role)
        val authorityGroupsIn = firstGroupAuthorities + secondGroupAuthorities
        assertEquals(authorityGroupsIn.toSet(), newAccountDomain.accountGroupMap.values.flatten().toSet())
    }

    @Test
    @Transactional
    @WithMockUser(username = "test@gmail.com", roles = ["SUPER_ADMIN"])
    fun `test admin creation fail`() {
        // GIVEN
        val authority = testAuthorityFactory.createAuthorities(em, listOf(Role(ROLE_ADMIN) to ADMIN_HIERARCHY)).first()
        val email = Email("test@gmail.com")
        val rawPassword = "test"
        val nickname = "RyanAtBurst"

        // WHEN, THEN
        val notCreatedGroupId = 1000000L
        val createdGroupIds = groups.map { it.id }.toSet()
        require(notCreatedGroupId !in createdGroupIds)
        assertThrows<AccountGroupPartiallyNotFoundException> {
            facadeAccountCrudService.createAdminAccount(
                email = email,
                rawPassword = rawPassword,
                nickname = nickname,
                groupIds = createdGroupIds + notCreatedGroupId
            )
        }
        em.clear()

        // THEN
        val accounts = em.createQuery("SELECT a FROM Account a WHERE a.email = :email", Account::class.java)
            .setParameter("email", email).resultList
        assertTrue(accounts.isEmpty())
    }
}
