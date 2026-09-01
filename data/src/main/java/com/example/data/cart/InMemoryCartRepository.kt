package com.example.data.cart

import com.example.domain.model.Cart
import com.example.domain.model.CartItem
import com.example.domain.repository.CartRepository
import java.math.BigDecimal

/** Демонстрационная корзина со встроенным набором позиций. */
class InMemoryCartRepository(
    private val items: List<CartItem> = DEMO_CART_ITEMS,
) : CartRepository {

    override suspend fun getCart(): Cart = Cart(items)

    companion object {
        val DEMO_CART_ITEMS: List<CartItem> = listOf(
            CartItem(productId = "p-1", name = "Notebook", unitPrice = BigDecimal("12.50"), quantity = 2),
            CartItem(productId = "p-2", name = "Pen", unitPrice = BigDecimal("3.20"), quantity = 3),
            CartItem(productId = "p-3", name = "Backpack", unitPrice = BigDecimal("29.99"), quantity = 1),
        )
    }
}
