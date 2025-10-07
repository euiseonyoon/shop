package com.example.shop.purchase.services

import com.example.shop.auth.domain.Account
import com.example.shop.auth.domain.Authority
import com.example.shop.auth.domain.Email
import com.example.shop.auth.domain.Role
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.auth.repositories.AuthorityRepository
import com.example.shop.common.TestPostgresqlContainer
import com.example.shop.constants.ROLE_PREFIX
import com.example.shop.products.domain.Category
import com.example.shop.products.domain.Product
import com.example.shop.products.respositories.CategoryRepository
import com.example.shop.products.respositories.ProductRepository
import com.example.shop.purchase.models.PurchaseDirectlyRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@SpringBootTest
class PurchaseServiceTest(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val purchaseService: PurchaseService,
    private val accountRepository: AccountRepository,
    private val authorityRepository: AuthorityRepository
) : TestPostgresqlContainer() {
    val INITIAL_PRODCUT_STOCK = 300
    lateinit var product: Product
    lateinit var account: Account

    @BeforeEach
    fun init() {
        val authority = Authority(Role("${ROLE_PREFIX}USER"), 100).also {
            authorityRepository.save(it)
        }
        account = Account(Email("purchaseServiceTest@gamil.com"), "1234", authority).also {
            accountRepository.save(it)
        }

        val category = Category("가구", null, "가구").also {
            categoryRepository.save(it)
        }

        product = Product("테이블", INITIAL_PRODCUT_STOCK, 10000, category).also {
            productRepository.save(it)
        }
    }

    @Test
    fun `test purchaseDirectly`() {
        // GIVEN
        val purchaseQuantity = 1
        val threadCount = 200
        val executorService = Executors.newFixedThreadPool(10) // 스레드 풀 생성
        val latch = CountDownLatch(threadCount)

        // WHEN
        for (i in 0 until threadCount) {
            executorService.submit {
                try {
                    val request = PurchaseDirectlyRequest(product.id, purchaseQuantity)
                    purchaseService.purchaseDirectly(request, account.id)
                } catch (e: Exception) {
                    throw e
                } finally {
                    latch.countDown()
                }
            }
        }
        latch.await(30, TimeUnit.SECONDS)
        Thread.sleep(1000 * 10)
        executorService.shutdown()
        // THEN
        val productDecremented = productRepository.findById(product.id).orElse(null)
        assertEquals(INITIAL_PRODCUT_STOCK - (purchaseQuantity * threadCount), productDecremented.stock)
    }
}
