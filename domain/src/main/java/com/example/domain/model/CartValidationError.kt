package com.example.domain.model

import java.math.BigDecimal

/**
 * Ошибка валидации позиции корзины.
 *
 * Каждая ошибка несёт идентификатор товара, поэтому пользователь видит,
 * какая именно позиция некорректна.
 */
sealed interface CartValidationError {

    /** Идентификатор товара позиции, в которой обнаружена ошибка. */
    val productId: String

    /** Количество единиц в позиции меньше 1. */
    data class InvalidQuantity(
        override val productId: String,
        val quantity: Int,
    ) : CartValidationError

    /** Цена за единицу меньше 0.00; сама цена 0.00 допустима. */
    data class InvalidPrice(
        override val productId: String,
        val unitPrice: BigDecimal,
    ) : CartValidationError
}
