package com.example.domain.model

/** Корзина — список позиций. */
data class Cart(
    val items: List<CartItem>,
) {

    /** unitCount — общее число единиц товара в корзине. */
    val unitCount: Int get() = items.sumOf { it.quantity }

    companion object {
        val EMPTY: Cart = Cart(emptyList())
    }
}
