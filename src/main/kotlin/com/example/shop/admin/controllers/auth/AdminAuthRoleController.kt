package com.example.shop.admin.controllers.auth

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AdminAuthRoleController.URI)
class AdminAuthRoleController {
    companion object {
        const val URI = AdminAuthController.URI + "/authority"
    }
    // 1. Authority 조회
    // 2. 생성
}
