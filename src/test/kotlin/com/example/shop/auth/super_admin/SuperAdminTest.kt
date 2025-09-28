package com.example.shop.auth.super_admin

import com.example.shop.auth.security.third_party.interfaces.ThirdPartyAuthenticationUserService
import com.example.shop.common.AccessTokenGetter
import com.example.shop.common.EasyAccessTokenTestConfig
import com.example.shop.constants.ADMIN_URI_PREFIX
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(EasyAccessTokenTestConfig::class)
class SuperAdminTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var accessTokenGetter: AccessTokenGetter

    @MockitoSpyBean(name = "googleUserService")
    private lateinit var googleOidcUserService: ThirdPartyAuthenticationUserService

    @Value("\${auth.super_admin}")
    lateinit var adminEmail: String

    @Test
    @Transactional
    fun `test super admin`() {
        // GIVEN
        Mockito.doReturn(adminEmail).`when`(googleOidcUserService).getEmailAddress(any())

        // WHEN & THEN
        mockMvc.perform(
            get("$ADMIN_URI_PREFIX/test")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", accessTokenGetter.getBearerToken(adminEmail))
        ).andExpect(status().isOk)
    }

}
