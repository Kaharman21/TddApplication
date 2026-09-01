package com.example.data.promo

import com.example.domain.model.PromoCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Локальный источник промокодов: один действующий и один просроченный, объявленные прямо в реализации.
 *
 * Репозиторий возвращает найденный промокод как есть — решение об истечении срока принимает
 * resolver, а не источник. Поэтому просроченный код тоже находится и возвращается с прошедшей
 * датой. Поиск нормализует ввод пользователя: обрезает пробелы и игнорирует регистр, чтобы
 * `  save10 ` находил тот же код, что и `SAVE10`. Репозиторий создаётся конструктором по
 * умолчанию и полагается на встроенный список кодов.
 */
class LocalPromoCodeRepositoryTest {

    private val repository = LocalPromoCodeRepository()

    @Test
    fun `the valid code is found with its percent and expiration date`() = runTest {
        val promoCode: PromoCode? = repository.findByCode("SAVE10")

        assertNotNull(promoCode)
        assertEquals("SAVE10", promoCode!!.code)
        assertEquals(10, promoCode.percent)
        assertEquals(LocalDate.of(2099, 12, 31), promoCode.expiresOn)
    }

    @Test
    fun `the expired code is found with a past expiration date`() = runTest {
        val promoCode: PromoCode? = repository.findByCode("OLD20")

        assertNotNull(promoCode)
        assertEquals("OLD20", promoCode!!.code)
        assertEquals(20, promoCode.percent)
        assertEquals(LocalDate.of(2020, 1, 1), promoCode.expiresOn)
    }

    @Test
    fun `an unknown code is not found`() = runTest {
        assertNull(repository.findByCode("NOPE"))
    }

    @Test
    fun `the lookup ignores case and surrounding whitespace`() = runTest {
        val promoCode: PromoCode? = repository.findByCode("  save10 ")

        assertNotNull(promoCode)
        assertEquals(10, promoCode!!.percent)
    }
}
