package com.example.shop.account.services

import com.example.shop.account.domain.Address
import com.example.shop.account.models.AddAddressRequest
import com.example.shop.account.repositories.AddressRepository
import com.example.shop.auth.models.AccountAuthenticationToken
import com.example.shop.auth.repositories.AccountRepository
import com.example.shop.common.apis.exceptions.BadRequestException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@Service
class AddressService(
    private val addressRepository: AddressRepository,
    private val accountRepository: AccountRepository
) {
    @Transactional
    fun addAddress(
        request: AddAddressRequest,
        authentication: Authentication,
    ): Address {
        val auth = authentication as AccountAuthenticationToken
        val account = accountRepository.findById(auth.accountId).orElseThrow(
            Supplier { BadRequestException("Account not found.") }
        )

        val address = Address()
        address.account = account
        address.detail = request.detail
        address.description = request.description

        return addressRepository.save(address)
    }
}
