package com.example.domain.model

import java.math.BigDecimal

/** Позиция корзины. */
data class CartItem(
    val productId: String,
    val name: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
)
