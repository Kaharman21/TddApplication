package com.example.domain.calculation

import com.example.domain.money.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * PromoDiscount — процент от базы (Subtotal) без округления: округление применяется один раз при
 * сборке итогового чека, поэтому сравнение идёт через `compareTo`.
 */
class PromoDiscountCalculatorTest {

    private val calculator = PromoDiscountCalculator()

    @Test
    fun `ten percent of fifty five is five and a half`() {
        val discount = calculator.discountOf(base = Money.of("55.00"), percent = 10)

        assertEquals(0, Money.of("5.50").compareTo(discount))
    }

    @Test
    fun `thirty three percent of ten is three and thirty`() {
        val discount = calculator.discountOf(base = Money.of("10.00"), percent = 33)

        assertEquals(0, Money.of("3.30").compareTo(discount))
    }

    @Test
    fun `one hundred percent discounts the whole base`() {
        val discount = calculator.discountOf(base = Money.of("37.49"), percent = 100)

        assertEquals(0, Money.of("37.49").compareTo(discount))
    }

    @Test
    fun `one percent of a base is a hundredth of it`() {
        val discount = calculator.discountOf(base = Money.of("250.00"), percent = 1)

        assertEquals(0, Money.of("2.50").compareTo(discount))
    }

    @Test
    fun `the discount is not rounded`() {
        val discount = calculator.discountOf(base = Money.of("10.01"), percent = 33)

        // Округление до scale 2 дало бы 3.30 — промежуточная скидка не округляется
        assertEquals(0, Money.of("3.3033").compareTo(discount))
        assertTrue(discount.scale() > Money.SCALE, "Discount scale was ${discount.scale()}")
    }
}
