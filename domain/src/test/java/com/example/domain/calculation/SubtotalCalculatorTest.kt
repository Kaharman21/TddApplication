package com.example.domain.calculation

import com.example.domain.model.Cart
import com.example.domain.model.CartItem
import com.example.domain.money.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * Subtotal — сумма произведений цены за единицу на количество по всем позициям.
 *
 * `subtotalOf` не округляет: округление выполняется один раз при сборке итогового чека.
 * Поэтому сравнение идёт через `compareTo` — `BigDecimal.equals` учитывает scale.
 */
class SubtotalCalculatorTest {

    private val calculator = SubtotalCalculator()

    @Test
    fun `subtotal of a single item cart is unit price multiplied by quantity`() {
        val cart = Cart(items = listOf(cartItem(unitPrice = "10.00", quantity = 3)))

        assertEquals(0, Money.of("30.00").compareTo(calculator.subtotalOf(cart)))
    }

    @Test
    fun `subtotal of a multi item cart is the sum of item products`() {
        val cart = Cart(
            items = listOf(
                cartItem(unitPrice = "10.00", quantity = 2),
                cartItem(unitPrice = "5.50", quantity = 3),
                cartItem(unitPrice = "0.99", quantity = 1),
            ),
        )

        // 20.00 + 16.50 + 0.99
        assertEquals(0, Money.of("37.49").compareTo(calculator.subtotalOf(cart)))
    }

    @Test
    fun `subtotal of an empty cart is zero`() {
        assertEquals(0, BigDecimal.ZERO.compareTo(calculator.subtotalOf(Cart.EMPTY)))
    }

    @Test
    fun `subtotal keeps full precision without rounding`() {
        val cart = Cart(items = listOf(cartItem(unitPrice = "0.005", quantity = 3)))

        val subtotal = calculator.subtotalOf(cart)

        // Округление до scale 2 дало бы 0.02 — промежуточная сумма не округляется
        assertEquals(0, Money.of("0.015").compareTo(subtotal))
        assertTrue(subtotal.scale() > Money.SCALE, "Subtotal scale was ${subtotal.scale()}")
    }

    private fun cartItem(
        unitPrice: String,
        quantity: Int,
        productId: String = "p-$unitPrice-$quantity",
    ): CartItem = CartItem(
        productId = productId,
        name = "Product $productId",
        unitPrice = Money.of(unitPrice),
        quantity = quantity,
    )
}
