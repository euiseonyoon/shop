package com.example.shop.auth.services

import com.example.shop.constants.ROLE_USER
import com.example.shop.auth.common.TestAccountGroupFactory
import com.example.shop.auth.common.TestAuthorityFactory
import com.example.shop.auth.common.TestGroupAuthorityFactory
import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.Email
import com.example.shop.auth.domain.GroupMember
import com.example.shop.auth.domain.Role
import com.example.shop.common.logger.LogSupport
import com.example.shop.constants.DEFAULT_USER_HIERARCHY
import com.example.shop.constants.ROLE_PREFIX
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class AccountServiceTest(
    @PersistenceContext
    private val em: EntityManager,
    private val testAuthorityFactory: TestAuthorityFactory,
    private val testGroupAuthorityFactory: TestGroupAuthorityFactory,
    private val testAccountGroupFactory: TestAccountGroupFactory,
    private val accountDomainService: AccountDomainService
) : LogSupport() {
    @Test
    @Transactional
    fun `test account repository extension`() {
        // GIVEN
        val authority = testAuthorityFactory.createAuthorities(em, listOf(Role(ROLE_USER) to DEFAULT_USER_HIERARCHY)).first()

        val groups = testAccountGroupFactory.createAccountGroup(em, listOf("group1", "group2"))
        val firstGroup = groups.first()
        val secondGroup = groups.last()

        val firstGroupAuthorities = testGroupAuthorityFactory.createGroupAuthorities(
            em,
            listOf(Role("${ROLE_PREFIX}authority_1-1"), Role("${ROLE_PREFIX}authority_1-2")),
            firstGroup
        )
        val secondGroupAuthorities = testGroupAuthorityFactory.createGroupAuthorities(
            em,
            listOf(Role("${ROLE_PREFIX}authority_2")),
            secondGroup
        )

        val testEmail = Email("test@gamil.com")
        val account = Account(
            email = testEmail,
            passwordHash = "123",
            authority = authority,
        ).also { em.persist(it) }
        em.flush()

        val groupMember = GroupMember(firstGroup, account).also { em.persist(it) }
        em.flush()
        em.clear()

        logger.info("==========================================")
        logger.info("==========================================")

        // WHEN
        val foundAccount = accountDomainService.findByEmail(testEmail)
        /**
         * Query Log:
         *      select
         *         distinct a1_0.id,
         *         a1_0.authority_id,
         *         a2_0.id,
         *         a2_0.role_name,
         *         a1_0.enabled,
         *         gmm1_0.account_id,
         *         gmm1_0.id,
         *         ag1_0.id,
         *         a3_0.account_group_id,
         *         a3_0.id,
         *         a3_0.name,
         *         ag1_0.name,
         *         a1_0.nickname,
         *         a1_0.oauth,
         *         a1_0.password,
         *         a1_0.email
         *     from
         *         account a1_0
         *     left join
         *         authority a2_0
         *             on a2_0.id=a1_0.authority_id
         *     left join
         *         group_member gmm1_0
         *             on a1_0.id=gmm1_0.account_id
         *     left join
         *         account_group ag1_0
         *             on ag1_0.id=gmm1_0.account_group_id
         *     left join
         *         group_authority a3_0
         *             on ag1_0.id=a3_0.account_group_id
         *     where
         *         a1_0.email=?
         * */

        // THEN
        assertNotNull(foundAccount)
        assertEquals(account, foundAccount.account)
        // 추가 query 발생 안함
        val foundGroupAuthoritiesNames = foundAccount.authorities.map { it.authority }.toSet()
        val foundAuthority = foundAccount.authority

        assertEquals(authority, foundAuthority)
        assertTrue {
            foundGroupAuthoritiesNames.containsAll(firstGroupAuthorities.map { it.role.name }.toSet())
        }
    }
}
