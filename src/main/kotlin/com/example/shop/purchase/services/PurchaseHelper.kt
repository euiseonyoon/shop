package com.example.shop.purchase.services

import com.example.shop.cart.domain.CartItem
import com.example.shop.common.apis.exceptions.NotFoundException
import com.example.shop.products.domain.Product
import com.example.shop.products.exceptions.ProductUnavailableException
import com.example.shop.purchase.domain.Purchase
import com.example.shop.purchase.enums.PurchaseStatus
import com.example.shop.purchase.exceptions.PurchaseByCartException
import com.example.shop.purchase.repositories.PurchaseRepository
import org.springframework.stereotype.Component

@Component
class PurchaseHelper(
    private val purchaseRepository: PurchaseRepository,
) {
    fun updatePurchaseStatus(purchase: Purchase, desiredStatus: PurchaseStatus) {
        if (purchase.status == PurchaseStatus.READY) {
            purchase.apply { this.updateStatus(desiredStatus) }.let { purchaseRepository.save(it) }
        }
    }

    fun filterProductOrThrow(product: Product?, quantity: Int): Product {
        val nonNullProduct = product ?: throw NotFoundException("Product Not found.")

        val (isAvailable, reason) = nonNullProduct.isPurchasable(quantity)
        if (!isAvailable) {
            throw ProductUnavailableException(nonNullProduct.id, reason)
        }

        return nonNullProduct
    }

    fun filterProductsOrThrow(
        cartItems: List<CartItem>,
        productsInCart: List<Product>,
    ): List<Pair<Product, Int>> {
        val errors = mutableListOf<ProductUnavailableException>()
        val productIdMap: Map<Long, Product> = productsInCart.associateBy { it.id }

        val availableProducts = cartItems.mapNotNull { cartItem ->
            val product = validateProduct(cartItem, productIdMap, errors)
            product?.let { Pair(it, cartItem.quantity) }
        }

        if (errors.isNotEmpty()) {
            throwPurchaseByCartException(errors)
        }
        return availableProducts
    }

    fun validateProduct(
        cartItem: CartItem,
        productIdMap: Map<Long, Product>,
        errors: MutableList<ProductUnavailableException>
    ): Product? {
        val product = productIdMap[cartItem.productId]
        if (product == null) {
            errors.add(ProductUnavailableException(cartItem.productId, "상품을 찾을 수 없음."))
            return null
        }

        val (isAvailable, reason) = product.isPurchasable(cartItem.quantity)
        return if (isAvailable) {
            product
        } else {
            errors.add(ProductUnavailableException(cartItem.productId, reason))
            null
        }
    }

    fun throwPurchaseByCartException(errors: List<ProductUnavailableException>) {
        val baseMessage = "장바구니 구매 오류. 상세 원인은 다음과 같습니다."
        val errorDetails = errors.joinToString(separator = ", ") { it.message }
        throw PurchaseByCartException("$baseMessage $errorDetails")
    }
}
