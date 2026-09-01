package com.example.domain.model

import com.example.domain.money.Money
import java.math.BigDecimal

/**
 * Итоговый чек расчёта корзины: суммы и статус промокода.
 */
data class Receipt(
    val subtotal: BigDecimal,
    val promoDiscount: BigDecimal,
    val discountedTotal: BigDecimal,
    val shippingCost: BigDecimal,
    val total: BigDecimal,
    val promoCodeStatus: PromoCodeStatus,
) {
    companion object {
        /** Пустой чек: все суммы равны нулю, статус промокода задаётся вызывающим кодом. */
        fun empty(promoCodeStatus: PromoCodeStatus = PromoCodeStatus.NotApplied): Receipt = Receipt(
            subtotal = Money.ZERO,
            promoDiscount = Money.ZERO,
            discountedTotal = Money.ZERO,
            shippingCost = Money.ZERO,
            total = Money.ZERO,
            promoCodeStatus = promoCodeStatus,
        )
    }
}
