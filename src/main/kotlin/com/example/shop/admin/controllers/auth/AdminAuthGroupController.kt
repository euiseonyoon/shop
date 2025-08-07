package com.example.shop.admin.controllers.auth

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AdminAuthGroupController.URI)
class AdminAuthGroupController {
    companion object {
        const val URI = AdminAuthController.URI + "/group"
    }
    // 1. Group 조회
    // 2. Group 생성
    // 3. Group 변경
}
