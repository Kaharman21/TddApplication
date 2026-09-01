package com.example.domain.calculation

import com.example.domain.model.PromoCode
import com.example.domain.model.PromoCodeStatus
import com.example.domain.repository.PromoCodeRepository
import com.example.domain.time.ClockProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Разрешение промокода в статус: единственный компонент, знающий про наличие кода и срок действия.
 *
 * «Сегодня» фиксируется константой и подставляется через Clock_Provider-лямбду, поэтому проверка
 * срока действия детерминирована. Граница проверяется отдельно: код, истекающий ровно сегодня,
 * ещё действует. Для null и кода с пробелами репозиторий не должен вызываться вовсе.
 */
class PromoCodeResolverTest {

    private val today = LocalDate.of(2026, 9, 1)
    private val clock = ClockProvider { today }
    private val repository = mockk<PromoCodeRepository>()
    private val resolver = PromoCodeResolver(repository, clock)

    @Test
    fun `a null code resolves to NotApplied without touching the repository`() = runTest {
        assertEquals(PromoCodeStatus.NotApplied, resolver.resolve(null))
        coVerify(exactly = 0) { repository.findByCode(any()) }
    }

    @Test
    fun `a blank code resolves to NotApplied without touching the repository`() = runTest {
        assertEquals(PromoCodeStatus.NotApplied, resolver.resolve(" "))
        assertEquals(PromoCodeStatus.NotApplied, resolver.resolve("\t"))
        assertEquals(PromoCodeStatus.NotApplied, resolver.resolve("\n"))
        coVerify(exactly = 0) { repository.findByCode(any()) }
    }

    @Test
    fun `an unknown code resolves to NotFound`() = runTest {
        coEvery { repository.findByCode(any()) } returns null

        assertEquals(PromoCodeStatus.NotFound("XXX"), resolver.resolve("XXX"))
    }

    @Test
    fun `a code expiring before today resolves to Expired`() = runTest {
        val expiresOn = today.minusDays(1)
        coEvery { repository.findByCode(any()) } returns promoCode(code = "OLD", expiresOn = expiresOn)

        assertEquals(PromoCodeStatus.Expired("OLD", expiresOn), resolver.resolve("OLD"))
    }

    @Test
    fun `a code expiring today is still applied`() = runTest {
        coEvery { repository.findByCode(any()) } returns promoCode(code = "NOW", percent = 15, expiresOn = today)

        assertEquals(PromoCodeStatus.Applied("NOW", 15), resolver.resolve("NOW"))
    }

    @Test
    fun `a code expiring after today is applied with its percent`() = runTest {
        coEvery { repository.findByCode(any()) } returns
            promoCode(code = "SAVE", percent = 10, expiresOn = today.plusDays(1))

        assertEquals(PromoCodeStatus.Applied("SAVE", 10), resolver.resolve("SAVE"))
    }

    @Test
    fun `a code is looked up once and trimmed before lookup`() = runTest {
        coEvery { repository.findByCode(any()) } returns promoCode(code = "SAVE10")

        resolver.resolve("  SAVE10  ")

        coVerify(exactly = 1) { repository.findByCode("SAVE10") }
    }

    private fun promoCode(
        code: String = "SAVE",
        percent: Int = 10,
        expiresOn: LocalDate = today.plusDays(1),
    ): PromoCode = PromoCode(code = code, percent = percent, expiresOn = expiresOn)
}
