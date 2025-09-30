package com.example.shop.products.respositories.extensions

import com.example.shop.products.domain.Category
import com.example.shop.products.respositories.CategoryRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class CategoryRepositoryExtensionImplTest(
    private val categoryRepository: CategoryRepository
) {

    lateinit var parentCategories: List<Category>
    lateinit var firstChildCategories: List<Category>
    lateinit var secondChildCategories: List<Category>


    @BeforeEach
    fun init() {
        parentCategories = listOf(
            Category("의류", null, "의류"),
            Category("식품", null, "식품"),
            Category("가구", null, "가구"),
        ).map { saveCategory(it) }

        firstChildCategories = listOf(
            Category("하의", parentCategories[0], "의류/하의"),
            Category("상의", parentCategories[0], "의류/상의"),
            Category("육류", parentCategories[1], "식품/육류"),
        ).map { saveCategory(it) }

        secondChildCategories = listOf(
            Category("청바지", firstChildCategories[0], "의류/하의/청바지"),
            Category("쇼츠", firstChildCategories[0], "의류/하의/쇼츠"),
            Category("셔츠", firstChildCategories[1], "의류/상의/셔츠"),
            Category("소고기", firstChildCategories[2], "식품/육류/소고기"),
        ).map { saveCategory(it) }
    }

    private fun saveCategory(category: Category): Category {
        categoryRepository.save(category)
        return category
    }

    @Test
    @Transactional
    fun `test searchWithIdsOrNames no criteria`() {
        // GIVEN
        val pageable = PageRequest.of(0, parentCategories.size, Sort.by("id"))

        // WHEN
        val results = categoryRepository.searchWithIdsOrNames(null, null, pageable).content

        // THEN
        assertEquals(parentCategories.size, results.size)
        parentCategories.zip(results).forEach { (category, result) ->
            assertNull(category.parent)
            assertEquals(category.name, result.name)
        }
    }

    @Test
    @Transactional
    fun `test searchWithIdsOrNames names`() {
        // GIVEN
        val pageable = PageRequest.of(0, 5, Sort.by("id"))

        // WHEN
        val targetNames = listOf("식품", "상의")
        val results = categoryRepository.searchWithIdsOrNames(targetNames, null, pageable).content

        // THEN
        assertEquals(targetNames.size, results.size)
        targetNames.zip(results).forEach { (name, result) ->
            assertEquals(name, result.name)
        }
    }

    @Test
    @Transactional
    fun `test searchWithIdsOrNames ids`() {
        // GIVEN
        val pageable = PageRequest.of(0, 5, Sort.by("id"))

        // WHEN
        val targetIds = listOf(parentCategories.last().id, secondChildCategories.first().id)
        val results = categoryRepository.searchWithIdsOrNames(null, targetIds, pageable).content

        // THEN
        assertEquals(targetIds.size, results.size)
        targetIds.zip(results).forEach { (id, result) ->
            assertEquals(id, result.id)
        }
    }

    @Test
    @Transactional
    fun `test searchWithIdsOrNames complex`() {
        // GIVEN
        val pageable = PageRequest.of(0, 3, Sort.by("id"))

        // WHEN
        val targetIds = listOf(firstChildCategories.first().id) // 하의
        val targetNames = listOf("상의", "하의", "소고기")
        val results = categoryRepository.searchWithIdsOrNames(targetNames, targetIds, pageable).content

        // THEN
        assertEquals(1, results.size)
        assertEquals(firstChildCategories.first(), results.first())
    }

    @Test
    @Transactional
    fun `test searchByIdIncludeChildren Include Child 1`() {
        // WHEN
        val targetId = parentCategories[0].id // 의류
        val results = categoryRepository.searchByIdIncludeChildren(targetId, true)

        // THEN : 의류, 하의, 상의, 청바지, 쇼츠, 셔츠
        assertEquals(6, results.size)
        assertTrue { results[0].name == "의류" }
        assertTrue { results[1].name == "하의" }
        assertTrue { results[2].name == "상의" }
        assertTrue { results[3].name == "청바지" }
        assertTrue { results[4].name == "쇼츠" }
        assertTrue { results[5].name == "셔츠" }
    }

    @Test
    @Transactional
    fun `test searchByIdIncludeChildren Include Child 2`() {
        // WHEN
        val targetId = parentCategories[2].id // 가구 - child 없음
        val results = categoryRepository.searchByIdIncludeChildren(targetId, true)

        // THEN : 가구
        assertEquals(1, results.size)
        assertTrue { results[0].name == "가구" }
    }

    @Test
    @Transactional
    fun `test searchByIdIncludeChildren Exclude Child 1`() {
        // WHEN
        val targetId = parentCategories[0].id // 의류 - child 있음
        val results = categoryRepository.searchByIdIncludeChildren(targetId, false)

        // THEN : 의류
        assertEquals(1, results.size)
        assertTrue { results.first().name == "의류" }
    }

    @Test
    @Transactional
    fun `test searchByIdIncludeChildren Exclude Child 2`() {
        // WHEN
        val targetId = parentCategories[2].id // 가구 - child 없음
        val results = categoryRepository.searchByIdIncludeChildren(targetId, false)

        // THEN : 가구
        assertEquals(1, results.size)
        assertTrue { results.first().name == "가구" }
    }

}
