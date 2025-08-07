package com.example.shop.auth.services.facades

import com.example.shop.constants.ROLE_ADMIN
import com.example.shop.constants.ROLE_USER
import com.example.shop.auth.common.TestAccountGroupFactory
import com.example.shop.auth.common.TestAuthorityFactory
import com.example.shop.auth.common.TestGroupAuthorityFactory
import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.auth.exceptions.AccountGroupPartiallyNotFoundException
import com.example.shop.constants.ADMIN_HIERARCHY
import com.example.shop.constants.DEFAULT_USER_HIERARCHY
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class FacadeAccountCrudServiceTest {
    @Autowired
    @PersistenceContext
    lateinit var em: EntityManager

    @Autowired
    lateinit var testAuthorityFactory: TestAuthorityFactory

    @Autowired
    lateinit var testAccountGroupFactory: TestAccountGroupFactory

    @Autowired
    lateinit var testGroupAuthorityFactory: TestGroupAuthorityFactory

    @Autowired
    lateinit var facadeAccountCrudService: FacadeAccountCrudService

    private val groupNames = listOf("group1", "group2")
    private var groups: List<AccountGroup> = listOf()
    private var firstGroupAuthorities: List<GroupAuthority> = listOf()
    private var secondGroupAuthorities: List<GroupAuthority> = listOf()

    @BeforeEach
    fun init() {
        val groups = testAccountGroupFactory.createAccountGroup(em, groupNames)
        this.groups = groups

        this.firstGroupAuthorities = testGroupAuthorityFactory.createGroupAuthorities(
            em,
            listOf("GROUP_authority_1-1", "GROUP_authority_1-2"),
            groups.first()
        )
        this.secondGroupAuthorities = testGroupAuthorityFactory.createGroupAuthorities(
            em,
            listOf("GROUP_authority_2"),
            groups.last()
        )
    }

    @Test
    @Transactional
    fun `test user creation success`() {
        // GIVEN
        val authority = testAuthorityFactory.createAuthorities(em, listOf(ROLE_USER to DEFAULT_USER_HIERARCHY)).first()
        val email = "test@gmail.com"
        val rawPassword = "test"
        val nickname = "RyanAtBurst"

        // WHEN
        val newUserAccount = facadeAccountCrudService.createUserAccount(
            email = email,
            rawPassword = rawPassword,
            nickname = nickname,
            thirdPartyOauthVendor = null,
            groupNames = setOf(groupNames.first())
        )
        em.clear()

        // THEN
        assertEquals(email, newUserAccount.email)
        assertEquals(nickname, newUserAccount.nickname)
        assertEquals(ROLE_USER, newUserAccount.authority!!.roleName)
        assertEquals(setOf(groups.first()), newUserAccount.getGroups().toSet())
        assertEquals(firstGroupAuthorities.toSet(), newUserAccount.getGroupAuthorities().toSet())
    }

    @Test
    @Transactional
    fun `test user creation fail`() {
        // GIVEN
        val authority = testAuthorityFactory.createAuthorities(em, listOf(ROLE_ADMIN to ADMIN_HIERARCHY)).first()
        val email = "test@gmail.com"
        val rawPassword = "test"
        val nickname = "RyanAtBurst"

        // WHEN
        facadeAccountCrudService.createUserAccount(
            email = email,
            rawPassword = rawPassword,
            nickname = nickname,
            thirdPartyOauthVendor = null,
            groupNames = setOf(groupNames.first())
        )

        // THEN
        em.clear()

        val accounts = em.createQuery("SELECT a FROM Account a WHERE a.email = :email", Account::class.java)
            .setParameter("email", email).resultList
        assertTrue(accounts.isEmpty())
    }

    @Test
    @Transactional
    fun `test admin creation success`() {
        // GIVEN
        val authority = testAuthorityFactory.createAuthorities(em, listOf(ROLE_ADMIN to ADMIN_HIERARCHY)).first()
        val email = "test@gmail.com"
        val rawPassword = "test"
        val nickname = "RyanAtBurst"

        // WHEN
        val newUserAccount = facadeAccountCrudService.createAdminAccount(
            email = email,
            rawPassword = rawPassword,
            nickname = nickname,
            groupNames = setOf(groupNames.first())
        )
        em.clear()

        // THEN
        assertEquals(email, newUserAccount.email)
        assertEquals(nickname, newUserAccount.nickname)
        assertEquals(ROLE_ADMIN, newUserAccount.authority!!.roleName)
        assertEquals(setOf(groups.first()), newUserAccount.getGroups().toSet())
        assertEquals(firstGroupAuthorities.toSet(), newUserAccount.getGroupAuthorities().toSet())
    }

    @Test
    @Transactional
    fun `test admin creation fail`() {
        // GIVEN
        val authority = testAuthorityFactory.createAuthorities(em, listOf(ROLE_ADMIN to ADMIN_HIERARCHY)).first()
        val email = "test@gmail.com"
        val rawPassword = "test"
        val nickname = "RyanAtBurst"

        // WHEN, THEN
        assertThrows<AccountGroupPartiallyNotFoundException> {
            facadeAccountCrudService.createAdminAccount(
                email = email,
                rawPassword = rawPassword,
                nickname = nickname,
                groupNames = setOf("not existing group name")
            )
        }

        // THEN
        em.clear()

        val accounts = em.createQuery("SELECT a FROM Account a WHERE a.email = :email", Account::class.java)
            .setParameter("email", email).resultList
        assertTrue(accounts.isEmpty())
    }
}