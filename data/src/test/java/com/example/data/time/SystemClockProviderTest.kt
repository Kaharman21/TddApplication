package com.example.data.time

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Системный провайдер даты.
 *
 * `today()` должен возвращать текущую системную дату. Чтобы тест не был флейки на границе
 * полуночи (дата может смениться между двумя вызовами `LocalDate.now()`), фиксируем дату до
 * и после вызова и проверяем, что результат попадает в диапазон `[before, after]`.
 */
class SystemClockProviderTest {

    @Test
    fun `today returns the current system date`() {
        val before = LocalDate.now()
        val result = SystemClockProvider().today()
        val after = LocalDate.now()

        assertTrue(!result.isBefore(before) && !result.isAfter(after))
    }
}
