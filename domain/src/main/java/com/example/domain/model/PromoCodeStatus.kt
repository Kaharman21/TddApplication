package com.example.domain.model

import java.time.LocalDate

/** Статус применения промокода в чеке. */
sealed interface PromoCodeStatus {

    /** Код не передан или состоит только из пробелов. */
    data object NotApplied : PromoCodeStatus

    /** Код найден и действует. */
    data class Applied(val code: String, val percent: Int) : PromoCodeStatus

    /** Код не найден в репозитории. */
    data class NotFound(val code: String) : PromoCodeStatus

    /** Срок действия кода истёк. */
    data class Expired(val code: String, val expiredOn: LocalDate) : PromoCodeStatus
}
