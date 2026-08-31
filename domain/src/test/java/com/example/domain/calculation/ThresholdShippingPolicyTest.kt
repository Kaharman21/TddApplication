package com.example.domain.calculation

import com.example.domain.money.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Стоимость доставки по порогу бесплатной доставки: сумма после скидки не меньше порога — доставка
 * бесплатна, иначе берётся стандартная стоимость.
 *
 * Проверяются именно границы порога (49.99 / 50.00 / 50.01): ошибка в знаке сравнения проявляется
 * только на точном совпадении с порогом, поэтому этот случай вынесен в отдельный тест.
 *
 * Пустая корзина за доставку не платит, даже когда сумма ниже порога — признаком пустоты служит
 * `unitCount`, равный нулю, а не сама сумма.
 *
 * Порог и стоимость — параметры конструктора, поэтому отдельный тест создаёт политику с другими
 * значениями и убеждается, что используются они, а не значения по умолчанию.
 *
 * Сравнение с порогом должно идти через `compareTo`: `BigDecimal("50.0")` и `BigDecimal("50.00")`
 * равны по значению, но не равны по `equals`, так как `equals` учитывает scale.
 */
class ThresholdShippingPolicyTest {

    private val policy = ThresholdShippingPolicy()

    @Test
    fun `a total at the threshold ships for free`() {
        val shippingCost = policy.shippingCostFor(discountedTotal = Money.of("50.00"), unitCount = 3)

        assertEquals(0, Money.of("0.00").compareTo(shippingCost))
    }

    @Test
    fun `a total just below the threshold costs the standard shipping`() {
        val shippingCost = policy.shippingCostFor(discountedTotal = Money.of("49.99"), unitCount = 3)

        assertEquals(0, Money.of("5.00").compareTo(shippingCost))
    }

    @Test
    fun `a total just above the threshold ships for free`() {
        val shippingCost = policy.shippingCostFor(discountedTotal = Money.of("50.01"), unitCount = 3)

        assertEquals(0, Money.of("0.00").compareTo(shippingCost))
    }

    @Test
    fun `an empty cart ships for free even below the threshold`() {
        val shippingCost = policy.shippingCostFor(discountedTotal = Money.of("0.00"), unitCount = 0)

        assertEquals(0, Money.of("0.00").compareTo(shippingCost))
    }

    @Test
    fun `custom threshold and cost passed to the constructor are used`() {
        val customPolicy = ThresholdShippingPolicy(
            freeShippingThreshold = Money.of("100.00"),
            standardShippingCost = Money.of("9.99"),
        )

        assertEquals(
            0,
            Money.of("9.99").compareTo(
                customPolicy.shippingCostFor(discountedTotal = Money.of("50.00"), unitCount = 1),
            ),
        )
        assertEquals(
            0,
            Money.of("0.00").compareTo(
                customPolicy.shippingCostFor(discountedTotal = Money.of("100.00"), unitCount = 1),
            ),
        )
    }

    @Test
    fun `the threshold comparison ignores scale`() {
        val shippingCost = policy.shippingCostFor(discountedTotal = Money.of("50.0"), unitCount = 1)

        assertEquals(0, Money.of("0.00").compareTo(shippingCost))
    }
}
