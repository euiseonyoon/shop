package com.example.shop.configurations.authorization

import com.example.shop.constants.ROLE_PREFIX
import com.example.shop.auth.security.super_admin.SuperAdminHttpSecurityExpressionHandler
import com.example.shop.auth.security.super_admin.SuperAdminMethodSecurityExpressionHandler
import com.example.shop.auth.services.AuthorityService
import com.example.shop.auth.utils.RoleHierarchyHelper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler

@Configuration
class RoleConfig {
    @Bean
    fun roleHierarchy(
        authorityService: AuthorityService,
        roleHierarchyHelper: RoleHierarchyHelper,
    ): RoleHierarchy {
        val roleNamesInHierarchyAsc = roleHierarchyHelper.getRoleNamesInHierarchyAsc()
        val hierarchyString = roleNamesInHierarchyAsc.joinToString(" > ")
        return RoleHierarchyImpl.fromHierarchy(hierarchyString)
    }

    /**
     * pre-post method security(@PreAuthorize, @PostAuthorize, @PreFilter, @PostFilter)를 사용한다면 추가해야한다.
     *
     * 이렇게 security expression을 핸들링 하는 핸들러의 구현체(AbstractSecurityExpressionHandler의 구현체)는 3개가 있다.
     *
     *      1.MethodSecurityExpressionHandler:
     *          메소드에서 Security expression을 사용하는 경우
     *      2.DefaultHttpSecurityExpressionHandler:
     *          Spring Security 6.x 이상 버전의 `HttpSecurity`.authorizeHttpRequests()에서 사용됨
     *      3. DefaultWebSecurityExpressionHandler:
     *           Spring Security 5.x 이하 버전의 `HttpSecurity`.authorizeRequests()에서 사용됨
     *
     * */
    @Bean
    fun methodSecurityExpressionHandler(
        roleHierarchy : RoleHierarchy,
        @Value("\${auth.super_admin}")
        superAdminEmail: String
    ): MethodSecurityExpressionHandler {
        val expressionHandler = SuperAdminMethodSecurityExpressionHandler(superAdminEmail).apply {
            setRoleHierarchy((roleHierarchy))
            setDefaultRolePrefix(ROLE_PREFIX)
        }
        return expressionHandler
    }

    @Bean
    fun httpSecurityExpressionHandler(
        roleHierarchy : RoleHierarchy,
        @Value("\${auth.super_admin}")
        superAdminEmail: String
    ): DefaultHttpSecurityExpressionHandler {
        val expressionHandler = SuperAdminHttpSecurityExpressionHandler(superAdminEmail).apply {
            setRoleHierarchy((roleHierarchy))
            setDefaultRolePrefix(ROLE_PREFIX)
        }
        return expressionHandler
    }
}
