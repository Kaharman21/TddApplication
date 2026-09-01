package com.example.data.promo

import com.example.domain.model.PromoCode
import com.example.domain.repository.PromoCodeRepository
import java.time.LocalDate

/** Локальный источник промокодов со встроенным списком. */
class LocalPromoCodeRepository(
    private val promoCodes: List<PromoCode> = LOCAL_PROMO_CODES,
) : PromoCodeRepository {

    override suspend fun findByCode(code: String): PromoCode? =
        promoCodes.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }

    companion object {
        const val VALID_CODE = "SAVE10"
        const val EXPIRED_CODE = "OLD20"

        val LOCAL_PROMO_CODES = listOf(
            PromoCode(VALID_CODE, 10, LocalDate.of(2099, 12, 31)),
            PromoCode(EXPIRED_CODE, 20, LocalDate.of(2020, 1, 1)),
        )
    }
}
