package com.example.shop.auth.services

import com.example.shop.auth.ROLE_PREFIX
import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.AccountGroup
import com.example.shop.auth.domain.Authority
import com.example.shop.auth.domain.GroupAuthority
import com.example.shop.auth.domain.GroupMember
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.common.logger.LogSupport
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
class AccountServiceTest : LogSupport() {
    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var accountService: AccountService

    @Autowired
    @PersistenceContext
    lateinit var em: EntityManager

    @Test
    @Transactional
    fun `test account repository extension`() {
        // GIVEN
        val EMAIL = "test@gamil.com"
        val authority = Authority("${ROLE_PREFIX}USER").also { em.persist(it) }
        em.flush()

        val firstGroup = AccountGroup("group1").also { em.persist(it) }
        val secondGroup = AccountGroup("group2").also { em.persist(it) }
        em.flush()

        val firstGroupAuthorities = listOf("GROUP_authority_1-1", "GROUP_authority_1-2").map {
            GroupAuthority(it).apply { accountGroup = firstGroup }.also { em.persist(it) }
        }
        val secondGroupAuthorities = listOf("GROUP_authority_2-1").map {
            GroupAuthority(it).apply { accountGroup = secondGroup }.also { em.persist(it) }
        }
        em.flush()

        val account = Account().apply {
            username = EMAIL
            password = "123"
            addRole(authority)
        }.also { em.persist(it) }
        em.flush()

        val groupMember = GroupMember(account, firstGroup).also { em.persist(it) }
        em.flush()
        em.clear()

        logger.info("==========================================")
        logger.info("==========================================")

        // WHEN
        val foundAccount = accountService.findWithAuthoritiesByEmail(EMAIL)
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
         *         a1_0.username
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
         *         a1_0.username=?
         * */

        // THEN
        assertNotNull(foundAccount)
        assertEquals(account, foundAccount)
        // 추가 query 발생 안함
        val foundGroupAuthorities = foundAccount.getGroupAuthorities()
        val foundAuthority = foundAccount.authority

        assertEquals(authority, foundAuthority)
        assertEquals(firstGroupAuthorities.toSet(), foundGroupAuthorities.toSet())
    }
}
