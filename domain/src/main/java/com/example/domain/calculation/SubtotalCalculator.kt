package com.example.domain.calculation

import com.example.domain.model.Cart
import java.math.BigDecimal

/**
 * Расчёт Subtotal — суммы произведений цены за единицу на количество по всем позициям корзины.
 *
 * Результат возвращается с полной точностью `BigDecimal`, без `setScale`: округление
 * до денежного scale выполняется один раз — при сборке итогового чека.
 */
class SubtotalCalculator {

    /**
     * Возвращает Subtotal корзины: сумму произведений `unitPrice` на `quantity` по всем позициям.
     * Для пустой корзины результат равен нулю.
     */
    fun subtotalOf(cart: Cart): BigDecimal =
        cart.items.fold(BigDecimal.ZERO) { accumulated, item ->
            accumulated + item.unitPrice * item.quantity.toBigDecimal()
        }
}
