package com.example.shop.admin.controllers.account

import com.example.shop.constants.ADMIN_ACCOUNT_URI_PREFIX
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ADMIN_ACCOUNT_URI_PREFIX)
class AdminAccountController {
    // 1. Account 정보 조회
    // 2. Account 롤 변경 + 그룹 변경
    // 3. Account enabled 변경
    // 4. Account 롤 변경 + 그룹 변경
}
