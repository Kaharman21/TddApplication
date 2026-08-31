package com.example.domain.calculation

import java.math.BigDecimal

interface ShippingPolicy {

    /**
     * @param discountedTotal уже округлённая сумма после скидки
     * @param unitCount число единиц в корзине
     */
    fun shippingCostFor(discountedTotal: BigDecimal, unitCount: Int): BigDecimal
}
