package com.example.domain.model

import java.math.BigDecimal

/**
 * Позиция корзины.
 *
 * @param productId идентификатор товара; попадает в ошибки валидации
 * @param name наименование для отображения на экране
 * @param unitPrice цена за единицу товара; `BigDecimal` — требование 1.4
 * @param quantity количество единиц товара в позиции
 */
data class CartItem(
    val productId: String,
    val name: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
)
