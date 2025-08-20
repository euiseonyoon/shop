package com.example.shop.account.controller

import com.example.shop.account.domain.Address
import com.example.shop.account.models.AddAddressRequest
import com.example.shop.account.services.AddressService
import com.example.shop.common.response.GlobalResponse
import com.example.shop.constants.ROLE_USER
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/account")
@PreAuthorize("hasRole('$ROLE_USER')")
class AccountController(
    private val addressService: AddressService,
) {
    @PostMapping("/address")
    fun addAddress(
        @RequestBody request: AddAddressRequest,
        authentication: Authentication,
    ): GlobalResponse<Address> {
        return addressService.addAddress(request, authentication).let {
            GlobalResponse.create(it)
        }
    }
}
