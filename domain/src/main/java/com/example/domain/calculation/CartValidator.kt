package com.example.domain.calculation

import com.example.domain.model.Cart
import com.example.domain.model.CartValidationError
import java.math.BigDecimal

/**
 * Валидация позиций корзины: количество не меньше 1, цена за единицу не меньше нуля.
 *
 * Проверяются все позиции, а не только первая некорректная: пользователю нужен полный
 * список проблем сразу.
 */
class CartValidator {

    fun validate(cart: Cart): List<CartValidationError> = cart.items.flatMap { item ->
        buildList {
            if (item.quantity < MIN_QUANTITY) {
                add(CartValidationError.InvalidQuantity(item.productId, item.quantity))
            }
            if (item.unitPrice < BigDecimal.ZERO) {
                add(CartValidationError.InvalidPrice(item.productId, item.unitPrice))
            }
        }
    }

    private companion object {
        const val MIN_QUANTITY: Int = 1
    }
}
