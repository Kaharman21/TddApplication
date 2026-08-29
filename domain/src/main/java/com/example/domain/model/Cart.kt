package com.example.domain.model

/** Корзина — список позиций. */
data class Cart(
    val items: List<CartItem>,
) {

    /** Unit_Count — общее число единиц товара в корзине. */
    val unitCount: Int get() = items.sumOf { it.quantity }

    companion object {

        /** Пустая корзина: Unit_Count равен 0, все суммы чека — 0.00. Требование 1.3. */
        val EMPTY: Cart = Cart(emptyList())
    }
}
