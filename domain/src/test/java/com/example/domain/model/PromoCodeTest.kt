package com.example.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

/**
 * Инвариант процента скидки задан в конструкторе Promo_Code: диапазон 1..100 — часть определения
 * промокода, поэтому объект с процентом вне диапазона просто не должен создаваться.
 *
 * Границы 1 и 100 проверяются отдельно: срабатывание проверки на границе исказило бы все
 * зависимые расчёты, поэтому обе допустимые крайние точки закрыты явным тестом.
 */
class PromoCodeTest {

    @Test
    fun `a percent below one is rejected`() {
        assertThrows<InvalidDiscountPercent> { promoCode(percent = 0) }
        assertThrows<InvalidDiscountPercent> { promoCode(percent = -5) }
    }

    @Test
    fun `a percent above one hundred is rejected`() {
        assertThrows<InvalidDiscountPercent> { promoCode(percent = 101) }
    }

    @Test
    fun `the boundary percents one and one hundred are accepted`() {
        assertEquals(1, promoCode(percent = 1).percent)
        assertEquals(100, promoCode(percent = 100).percent)
    }

    private fun promoCode(
        percent: Int,
        code: String = "SAVE",
        expiresOn: LocalDate = LocalDate.of(2099, 12, 31),
    ): PromoCode = PromoCode(code = code, percent = percent, expiresOn = expiresOn)
}
