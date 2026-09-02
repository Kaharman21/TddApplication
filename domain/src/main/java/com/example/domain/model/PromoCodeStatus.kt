package com.example.domain.model

import java.time.LocalDate

/** Статус применения промокода в чеке. */
sealed interface PromoCodeStatus {

    /** Код не передан или состоит только из пробелов. */
    data object NotApplied : PromoCodeStatus
    data class Applied(val code: String, val percent: Int) : PromoCodeStatus
    data class NotFound(val code: String) : PromoCodeStatus
    data class Expired(val code: String, val expiredOn: LocalDate) : PromoCodeStatus
}
