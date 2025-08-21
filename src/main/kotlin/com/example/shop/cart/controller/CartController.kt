package com.example.shop.cart.controller

import com.example.shop.cart.models.AddToCartRequest
import com.example.shop.cart.models.CartDto
import com.example.shop.cart.models.RemoveFromCartRequest
import com.example.shop.cart.models.UpdateCartQuantityRequest
import com.example.shop.cart.models.toDto
import com.example.shop.cart.services.CartItemService
import com.example.shop.cart.services.CartService
import com.example.shop.common.response.GlobalResponse
import com.example.shop.constants.ROLE_USER
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cart")
@PreAuthorize("hasRole('$ROLE_USER')")
class CartController(
    private val cartService: CartService,
    private val cartItemService: CartItemService,
) {
    @GetMapping
    fun getMyCart(authentication: Authentication): GlobalResponse<CartDto?> {
        return cartService.getMyCart(authentication).let {
            GlobalResponse.create(it?.toDto())
        }
    }

    @PostMapping
    fun addToCart(
        @RequestBody @Valid request: AddToCartRequest,
        authentication: Authentication,
    ): GlobalResponse<CartDto> {
        return cartItemService.addItemToCart(request, authentication).let {
            GlobalResponse.create(it.toDto())
        }
    }

    @DeleteMapping
    fun removeFromCart(
        @RequestBody request: RemoveFromCartRequest,
        authentication: Authentication,
    ): GlobalResponse<CartDto?> {
        return cartItemService.removeItemFromCart(request, authentication).let {
            GlobalResponse.create(it?.toDto())
        }
    }

    @PatchMapping
    fun updateQuantity(
        @RequestBody @Valid request: UpdateCartQuantityRequest,
        authentication: Authentication,
    ): GlobalResponse<CartDto?> {
        return cartItemService.updateQuantity(request, authentication).let {
            GlobalResponse.create(it?.toDto())
        }
    }
}
