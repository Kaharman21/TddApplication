package com.example.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * Пустой чек — начальное состояние расчёта: все денежные поля равны нулю,
 * а статус промокода задаётся вызывающим кодом.
 */
class ReceiptTest {

    @Test
    fun `an empty receipt has zero amounts and a not applied promo status`() {
        val receipt = Receipt.empty()

        // Числовое равенство BigDecimal проверяется через compareTo:
        // equals у BigDecimal учитывает scale, а нас интересует именно значение.
        assertEquals(0, BigDecimal.ZERO.compareTo(receipt.subtotal))
        assertEquals(0, BigDecimal.ZERO.compareTo(receipt.promoDiscount))
        assertEquals(0, BigDecimal.ZERO.compareTo(receipt.discountedTotal))
        assertEquals(0, BigDecimal.ZERO.compareTo(receipt.shippingCost))
        assertEquals(0, BigDecimal.ZERO.compareTo(receipt.total))
        assertEquals(PromoCodeStatus.NotApplied, receipt.promoCodeStatus)
    }

    @Test
    fun `an empty receipt uses the given promo code status`() {
        val status = PromoCodeStatus.NotFound("XXX")

        val receipt = Receipt.empty(status)

        assertEquals(status, receipt.promoCodeStatus)
        assertEquals(0, BigDecimal.ZERO.compareTo(receipt.subtotal))
        assertEquals(0, BigDecimal.ZERO.compareTo(receipt.promoDiscount))
        assertEquals(0, BigDecimal.ZERO.compareTo(receipt.discountedTotal))
        assertEquals(0, BigDecimal.ZERO.compareTo(receipt.shippingCost))
        assertEquals(0, BigDecimal.ZERO.compareTo(receipt.total))
    }
}
