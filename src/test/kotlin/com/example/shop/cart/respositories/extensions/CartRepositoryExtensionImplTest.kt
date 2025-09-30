package com.example.shop.cart.respositories.extensions

import com.example.shop.auth.common.TestAuthorityFactory
import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.Email
import com.example.shop.auth.domain.Role
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.cart.domain.Cart
import com.example.shop.cart.respositories.CartRepository
import com.example.shop.constants.ROLE_USER
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@SpringBootTest
class CartRepositoryExtensionImplTest(
    private val cartRepository: CartRepository,
    private val authorityFactory: TestAuthorityFactory,
    private val accountRepository: AccountRepository,
    private val em: EntityManager,
) {
    lateinit var accounts: List<Account>
    lateinit var carts: List<Cart>

    @BeforeTest
    fun init() {
        val authority = authorityFactory.createAuthorities(em, listOf(Role(ROLE_USER) to 100)).first()
        accounts = listOf(
            Account(Email("first@gmail.com"), "first", authority),
            Account(Email("second@gmail.com"), "second", authority),
            Account(Email("third@gmail.com"), "third", authority),
        ).map {
            accountRepository.save(it)
            it
        }

        carts = listOf(
            Cart(accounts[0].id, true),
            Cart(accounts[0].id, false),
            Cart(accounts[1].id, true),
        ).map {
            cartRepository.save(it)
            it
        }
    }

    @Test
    @Transactional
    fun `test 1`() {
        // GIVEN
        val accountId = accounts[0].id

        // WHEN
        val result = cartRepository.getNotPurchasedCartByAccountId(accountId)

        // THEN
        assertEquals(carts[1], result)
    }

    @Test
    @Transactional
    fun `test 2`() {
        // GIVEN
        val accountId = accounts[1].id

        // WHEN
        val result = cartRepository.getNotPurchasedCartByAccountId(accountId)

        // THEN
        assertNull(result)
    }

    @Test
    @Transactional
    fun `test 3`() {
        // GIVEN
        val accountId = accounts[2].id

        // WHEN
        val result = cartRepository.getNotPurchasedCartByAccountId(accountId)

        // THEN
        assertNull(result)
    }
}
