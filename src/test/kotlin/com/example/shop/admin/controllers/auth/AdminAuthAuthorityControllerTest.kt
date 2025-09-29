package com.example.shop.admin.controllers.auth

import com.example.shop.admin.models.auth.AuthorityCreateRequest
import com.example.shop.auth.common.AuthTestUtil
import com.example.shop.auth.domain.Role
import com.example.shop.auth.repositories.AuthorityRepository
import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import com.example.shop.auth.services.AuthorityService
import com.example.shop.auth.utils.RoleHierarchyHelper
import com.example.shop.common.AccessTokenGetter
import com.example.shop.common.EasyAccessTokenTestConfig
import com.example.shop.constants.ROLE_PREFIX
import com.example.shop.constants.ROLE_SUPER_ADMIN
import com.example.shop.constants.SUPER_ADMIN_NAME
import com.example.shop.redis.authority_refresh.AuthorityRefreshMessageSubscriber
import jakarta.persistence.EntityManager
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A
import kotlin.test.assertTrue

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(EasyAccessTokenTestConfig::class, TestRedisContainerConfig::class)
class AdminAuthAuthorityControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var json: Json

    @Autowired
    lateinit var accessTokenGetter: AccessTokenGetter

    @Autowired
    lateinit var roleHierarchyHelper: RoleHierarchyHelper

    @Autowired
    lateinit var authorityRepository: AuthorityRepository

    @MockitoSpyBean(name = "googleUserService")
    lateinit var googleOidcUserService: ThirdPartyAuthenticationUserService

    @MockitoSpyBean
    lateinit var authorityRefreshMessageSubscriber: AuthorityRefreshMessageSubscriber

    @Value("\${auth.super_admin}")
    lateinit var adminEmail: String

    @Test
    @Transactional
    fun `test authority create`() {
        // GIVEN
        Mockito.doReturn(adminEmail).`when`(googleOidcUserService).getEmailAddress(any())
        val roleToCreate = Role(ROLE_PREFIX + "STAFF")
        val roleHierarchy = 5
        assertTrue { roleToCreate !in roleHierarchyHelper.getRoleHierarchyMap().keys }

        // WHEN & THEN
        val bearerToken = accessTokenGetter.getBearerToken(adminEmail)
        val requestBodyJson = json.encodeToString(
            AuthorityCreateRequest.serializer(),
            AuthorityCreateRequest(roleToCreate.name, roleHierarchy),
        )
        Mockito.reset(authorityRefreshMessageSubscriber)
        AuthTestUtil.makePostCall(
            mockMvc,
            AdminAuthRoleController.URI,
            requestBodyJson,
            mapOf("Authorization" to bearerToken)
        ).andExpect(status().isOk)

        // THEN
        verify(authorityRefreshMessageSubscriber, times(1)).handleAuthorityRefreshMessage(any(), any())
        val authoritiesFromDb = authorityRepository.findAll()
        assertNotNull { authoritiesFromDb.find { it.role == roleToCreate } }
    }
}
