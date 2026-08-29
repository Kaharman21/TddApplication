package com.example.domain.money

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * О сравнении BigDecimal: `BigDecimal("50.0")` не равен `BigDecimal("50.00")` по `equals`,
 * так как `equals` сравнивает не только значение, но и scale. Поэтому числовое равенство
 * проверяется через `compareTo`
 */
class MoneyTest {

    @Test
    fun `round applies HALF_UP on an exact half`() {
        val rounded = Money.round(BigDecimal("49.995"))
        assertEquals(0, BigDecimal("50.00").compareTo(rounded))
    }

    @Test
    fun `round applies HALF_UP below an exact half`() {
        val rounded = Money.round(BigDecimal("3.294"))
        assertEquals(0, BigDecimal("3.29").compareTo(rounded))
    }

    @Test
    fun `ZERO is zero with money scale`() {
        assertEquals(0, BigDecimal.ZERO.compareTo(Money.ZERO))
        assertEquals(Money.SCALE, Money.ZERO.scale())
    }

    @Test
    fun `SCALE is two decimal places`() {
        assertEquals(2, Money.SCALE)
    }

    @Test
    fun `round always returns a value with money scale`() {
        val values = listOf(
            Money.of("0"),
            Money.of("7"),
            Money.of("1.5"),
            Money.of("49.995"),
            Money.of("12.3456789"),
        )

        values.forEach { value ->
            assertEquals(Money.SCALE, Money.round(value).scale())
        }
    }
}
