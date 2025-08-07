package com.example.shop.configurations.authorization

import com.example.shop.constants.ADMIN_NAME
import com.example.shop.constants.ROLE_PREFIX
import com.example.shop.constants.SUPER_ADMIN_NAME
import com.example.shop.constants.USER_NAME
import com.example.shop.auth.security.super_admin.SuperAdminHttpSecurityExpressionHandler
import com.example.shop.auth.security.super_admin.SuperAdminMethodSecurityExpressionHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler

@Configuration
class RoleConfig {
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
        @Value("\${auth.super_admin}")
        superAdminEmail: String
    ): MethodSecurityExpressionHandler {
        val expressionHandler = SuperAdminMethodSecurityExpressionHandler(superAdminEmail).apply {
            setRoleHierarchy((roleHierarchy()))
            setDefaultRolePrefix(ROLE_PREFIX)
        }
        return expressionHandler
    }

    @Bean
    fun httpSecurityExpressionHandler(
        @Value("\${auth.super_admin}")
        superAdminEmail: String
    ): DefaultHttpSecurityExpressionHandler {
        val expressionHandler = SuperAdminHttpSecurityExpressionHandler(superAdminEmail).apply {
            setRoleHierarchy((roleHierarchy()))
            setDefaultRolePrefix(ROLE_PREFIX)
        }
        return expressionHandler
    }

    /**
     * 참고: https://docs.spring.io/spring-security/reference/servlet/authorization/architecture.html#authz-hierarchical-roles
     *
     * static bean
     * 장점:
     *      static @Bean은 스프링 컨테이너 초기화 시점에 다른 빈보다 먼저 등록되어, 빈 설정 정보 조작이나 초기화 로직 실행에 유용합니다.
     * 주의점:
     *      static @Bean 메서드에서는 스프링 컨테이너가 관리하는 빈에 대한 의존성 주입을 받을 수 없습니다. 필요한 빈은 static 변수나 BeanFactory를 통해 직접 가져와야 합니다.
     * */
    companion object {
        @JvmStatic
        @Bean
        fun roleHierarchy(): RoleHierarchy {
            return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(SUPER_ADMIN_NAME).implies(ADMIN_NAME)
                .role(ADMIN_NAME).implies(USER_NAME)
                .build()
        }
    }
}
