package com.example.domain.calculation

import java.math.BigDecimal

/**
 * Процент от базы (Subtotal) без округления: округление применяется один раз при сборке чека.
 */
class PromoDiscountCalculator {

    // Сначала умножение, затем сдвиг точки на два знака — деление на 100 остаётся точным
    fun discountOf(base: BigDecimal, percent: Int): BigDecimal =
        base.multiply(percent.toBigDecimal()).movePointLeft(2)
}
