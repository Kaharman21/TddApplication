package com.example.domain.usecase

import com.example.domain.calculation.CartValidator
import com.example.domain.calculation.PromoCodeResolver
import com.example.domain.calculation.PromoDiscountCalculator
import com.example.domain.calculation.ShippingPolicy
import com.example.domain.calculation.SubtotalCalculator
import com.example.domain.model.BasketCalculationResult
import com.example.domain.model.Cart
import com.example.domain.model.PromoCodeStatus
import com.example.domain.model.Receipt
import com.example.domain.money.Money
import java.math.BigDecimal

/**
 * Оркестрация расчёта корзины: единственное место, где зафиксирован порядок правил
 * и где происходит округление денежных полей чека.
 */
class CalculateBasketUseCase(
    private val cartValidator: CartValidator,
    private val subtotalCalculator: SubtotalCalculator,
    private val promoCodeResolver: PromoCodeResolver,
    private val promoDiscountCalculator: PromoDiscountCalculator,
    private val shippingPolicy: ShippingPolicy,
) {

    suspend operator fun invoke(cart: Cart, promoCode: String?): BasketCalculationResult {
        // При ошибках валидации расчёт не выполняется
        val errors = cartValidator.validate(cart)
        if (errors.isNotEmpty()) return BasketCalculationResult.Failure(errors)

        // Точный Subtotal без округления — база для процента скидки
        val subtotal = subtotalCalculator.subtotalOf(cart)

        val status = promoCodeResolver.resolve(promoCode)

        // Скидка считается только для действующего кода, база — точный Subtotal
        val promoDiscount = if (status is PromoCodeStatus.Applied) {
            promoDiscountCalculator.discountOf(subtotal, status.percent)
        } else {
            BigDecimal.ZERO
        }

        // Discounted_Total ограничен снизу нулём; округляется перед определением доставки
        val exactDiscountedTotal = (subtotal - promoDiscount).coerceAtLeast(BigDecimal.ZERO)
        val roundedDiscountedTotal = Money.round(exactDiscountedTotal)

        // В политику доставки уходит округлённый Discounted_Total
        val shippingCost = shippingPolicy.shippingCostFor(roundedDiscountedTotal, cart.unitCount)

        val receipt = Receipt(
            subtotal = Money.round(subtotal),
            promoDiscount = Money.round(promoDiscount),
            discountedTotal = roundedDiscountedTotal,
            shippingCost = Money.round(shippingCost),
            total = Money.round(roundedDiscountedTotal + shippingCost),
            promoCodeStatus = status,
        )
        return BasketCalculationResult.Success(receipt)
    }
}
