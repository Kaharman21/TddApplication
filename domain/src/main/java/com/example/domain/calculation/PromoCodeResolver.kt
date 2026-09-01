package com.example.domain.calculation

import com.example.domain.model.PromoCodeStatus
import com.example.domain.repository.PromoCodeRepository
import com.example.domain.time.ClockProvider

/**
 * Единственный компонент, знающий про наличие кода и срок действия.
 * Пустой или код с пробелом в репозиторий не уходит.
 */
class PromoCodeResolver(
    private val promoCodeRepository: PromoCodeRepository,
    private val clockProvider: ClockProvider,
) {
    suspend fun resolve(rawCode: String?): PromoCodeStatus {
        if (rawCode.isNullOrBlank()) return PromoCodeStatus.NotApplied

        val code = rawCode.trim()
        val promoCode = promoCodeRepository.findByCode(code)
            ?: return PromoCodeStatus.NotFound(code)

        // Срок действия истекает включительно в день окончания, поэтому < today — уже истёк
        return if (promoCode.expiresOn < clockProvider.today()) {
            PromoCodeStatus.Expired(code, promoCode.expiresOn)
        } else {
            PromoCodeStatus.Applied(code, promoCode.percent)
        }
    }
}
