package com.example.domain.model

import com.example.domain.money.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Спецификация моделей корзины: Unit_Count выводится из количеств позиций,
 * а пустая корзина является корректно определённым значением. Требование 1.3.
 */
class CartTest {

    @Test
    fun `unitCount is the sum of item quantities`() {
        val cart = Cart(
            items = listOf(
                cartItem(productId = "p1", quantity = 2),
                cartItem(productId = "p2", quantity = 3),
                cartItem(productId = "p3", quantity = 1),
            ),
        )

        assertEquals(6, cart.unitCount)
    }

    @Test
    fun `unitCount of a single item cart equals that item quantity`() {
        val cart = Cart(items = listOf(cartItem(productId = "p1", quantity = 4)))

        assertEquals(4, cart.unitCount)
    }

    @Test
    fun `EMPTY cart has no units`() {
        assertEquals(0, Cart.EMPTY.unitCount)
    }

    @Test
    fun `EMPTY cart has no items`() {
        assertTrue(Cart.EMPTY.items.isEmpty())
    }

    private fun cartItem(
        productId: String,
        quantity: Int,
        unitPrice: String = "10.00",
    ): CartItem = CartItem(
        productId = productId,
        name = "Product $productId",
        unitPrice = Money.of(unitPrice),
        quantity = quantity,
    )
}
