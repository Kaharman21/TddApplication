package com.example.domain.model

import java.time.LocalDate

/** Процент скидки вне диапазона 1..100 недопустим. */
class InvalidDiscountPercent(val percent: Int) :
    IllegalArgumentException("Discount percent must be in 1..100 but was $percent")

/**
 * @param percent процент скидки, 1..100
 * @param expiresOn последний день действия включительно
 */
data class PromoCode(
    val code: String,
    val percent: Int,
    val expiresOn: LocalDate,
) {
    init {
        if (percent !in VALID_PERCENT_RANGE) throw InvalidDiscountPercent(percent)
    }

    companion object {
        val VALID_PERCENT_RANGE: IntRange = 1..100
    }
}
