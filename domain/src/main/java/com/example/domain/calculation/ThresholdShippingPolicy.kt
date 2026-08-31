package com.example.domain.calculation

import com.example.domain.money.Money
import java.math.BigDecimal

/**
 * Доставка бесплатна, когда сумма после скидки достигла порога, иначе берётся стандартная стоимость.
 * Пустая корзина за доставку не платит.
 */
class ThresholdShippingPolicy(
    private val freeShippingThreshold: BigDecimal = DEFAULT_FREE_SHIPPING_THRESHOLD,
    private val standardShippingCost: BigDecimal = DEFAULT_STANDARD_SHIPPING_COST,
) : ShippingPolicy {

    override fun shippingCostFor(discountedTotal: BigDecimal, unitCount: Int): BigDecimal = when {
        unitCount == 0 -> Money.ZERO
        discountedTotal >= freeShippingThreshold -> Money.ZERO
        else -> standardShippingCost
    }

    companion object {
        val DEFAULT_FREE_SHIPPING_THRESHOLD: BigDecimal = Money.of("50.00")
        val DEFAULT_STANDARD_SHIPPING_COST: BigDecimal = Money.of("5.00")
    }
}
