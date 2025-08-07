package com.example.shop.admin.controllers.auth

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AdminAuthGroupAuthorityController.URI)
class AdminAuthGroupAuthorityController {
    companion object {
        const val URI = AdminAuthController.URI + "/group-authority"
    }
    // 1. Group Authority 조회
    // 1. Group Authority 생성
    // 1. Group Authority 삭제
}
