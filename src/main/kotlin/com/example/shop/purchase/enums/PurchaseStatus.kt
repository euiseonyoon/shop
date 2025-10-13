package com.example.shop.purchase.enums

enum class PurchaseStatus {
    READY,
    APPROVED, // 토스 페이먼츠에 approve 후의 상태
    FAILED, // 토스 페이먼츠에서 fail 후 상태
    STOCK_INSUFFICIENT, // 상품 재고 차감 과정에서 "재고 개수 부족" 이유로 적절한 주문이 아닌 상태
    STOCK_NOT_UPDATED_IN_TIME,
}
