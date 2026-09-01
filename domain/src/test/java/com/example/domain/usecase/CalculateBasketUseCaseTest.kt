package com.example.domain.usecase

import com.example.domain.calculation.CartValidator
import com.example.domain.calculation.PromoCodeResolver
import com.example.domain.calculation.PromoDiscountCalculator
import com.example.domain.calculation.ShippingPolicy
import com.example.domain.calculation.SubtotalCalculator
import com.example.domain.calculation.ThresholdShippingPolicy
import com.example.domain.model.BasketCalculationResult
import com.example.domain.model.Cart
import com.example.domain.model.CartItem
import com.example.domain.model.CartValidationError
import com.example.domain.model.PromoCode
import com.example.domain.model.PromoCodeStatus
import com.example.domain.money.Money
import com.example.domain.repository.PromoCodeRepository
import com.example.domain.time.ClockProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Оркестрация расчёта корзины: единственное место, где зафиксирован порядок правил
 * (промокод от Subtotal → Discounted_Total как разность → доставка по округлённому
 * Discounted_Total) и где происходит округление денежных полей чека.
 *
 * Тесты порядка правил и статусов работают на моках компонентов (`PromoCodeResolver`,
 * `PromoDiscountCalculator`, `ShippingPolicy`), чтобы проверять именно логику
 * оркестрации, а не арифметику. Сквозные примеры собирают реальные компоненты и мокают
 * только внешние зависимости (`PromoCodeRepository`, `ClockProvider`), поэтому проверяют
 * согласованную работу всех правил на конкретных числах.
 *
 * Числовое равенство `BigDecimal` проверяется через `compareTo` (`BigDecimal.equals`
 * учитывает scale), а форма результата (scale) — отдельным утверждением.
 */
class CalculateBasketUseCaseTest {

    private val today = LocalDate.of(2025, 1, 15)

    // Реальные чистые компоненты, у которых нет внешних зависимостей
    private val cartValidator = CartValidator()
    private val subtotalCalculator = SubtotalCalculator()

    // ----- Тесты порядка правил и статусов (моки компонентов) -----

    @Test
    fun `an invalid cart fails with validation errors and no calculation`() = runTest {
        val promoCodeResolver = mockk<PromoCodeResolver>()
        val promoDiscountCalculator = mockk<PromoDiscountCalculator>()
        val shippingPolicy = mockk<ShippingPolicy>()
        val useCase = CalculateBasketUseCase(
            cartValidator = cartValidator,
            subtotalCalculator = subtotalCalculator,
            promoCodeResolver = promoCodeResolver,
            promoDiscountCalculator = promoDiscountCalculator,
            shippingPolicy = shippingPolicy,
        )
        val cart = Cart(items = listOf(cartItem(unitPrice = "10.00", quantity = 0, productId = "bad")))

        val result = useCase(cart, promoCode = null)

        assertTrue(result is BasketCalculationResult.Failure, "Ожидался Failure, был $result")
        val errors = (result as BasketCalculationResult.Failure).errors
        assertEquals(listOf(CartValidationError.InvalidQuantity("bad", 0)), errors)
        // При ошибках валидации расчёт не выполняется — компоненты расчёта не вызываются
        coVerify(exactly = 0) { promoCodeResolver.resolve(any()) }
        coVerify(exactly = 0) { promoDiscountCalculator.discountOf(any(), any()) }
        coVerify(exactly = 0) { shippingPolicy.shippingCostFor(any(), any()) }
    }

    @Test
    fun `an empty cart produces a receipt with zero amounts and a not applied promo status`() = runTest {
        val useCase = realUseCase()

        val result = useCase(Cart.EMPTY, promoCode = null)

        val receipt = (result as BasketCalculationResult.Success).receipt
        assertEquals(0, Money.ZERO.compareTo(receipt.subtotal))
        assertEquals(0, Money.ZERO.compareTo(receipt.promoDiscount))
        assertEquals(0, Money.ZERO.compareTo(receipt.discountedTotal))
        assertEquals(0, Money.ZERO.compareTo(receipt.shippingCost))
        assertEquals(0, Money.ZERO.compareTo(receipt.total))
        assertEquals(PromoCodeStatus.NotApplied, receipt.promoCodeStatus)
    }

    @Test
    fun `the promo discount base is the subtotal`() = runTest {
        val promoCodeResolver = mockk<PromoCodeResolver>()
        val promoDiscountCalculator = mockk<PromoDiscountCalculator>()
        val shippingPolicy = mockk<ShippingPolicy>()
        coEvery { promoCodeResolver.resolve(any()) } returns PromoCodeStatus.Applied("SAVE10", 10)
        val baseSlot = slot<BigDecimal>()
        coEvery { promoDiscountCalculator.discountOf(capture(baseSlot), any()) } returns Money.of("3.00")
        coEvery { shippingPolicy.shippingCostFor(any(), any()) } returns Money.ZERO
        val useCase = CalculateBasketUseCase(
            cartValidator = cartValidator,
            subtotalCalculator = subtotalCalculator,
            promoCodeResolver = promoCodeResolver,
            promoDiscountCalculator = promoDiscountCalculator,
            shippingPolicy = shippingPolicy,
        )
        // 3 шт по 10.00 → subtotal 30.00
        val cart = Cart(items = listOf(cartItem(unitPrice = "10.00", quantity = 3)))

        useCase(cart, promoCode = "SAVE10")

        // База процента скидки — это Subtotal корзины
        assertEquals(0, Money.of("30.00").compareTo(baseSlot.captured))
    }

    @Test
    fun `three units of ten with a ten percent code give thirty three and twenty seven`() = runTest {
        val useCase = realUseCase(promoCode = promoCode(code = "SAVE10", percent = 10))
        // 3 шт по 10.00 → subtotal 30.00, скидка 10% = 3.00, discountedTotal 27.00
        val cart = Cart(items = listOf(cartItem(unitPrice = "10.00", quantity = 3)))

        val result = useCase(cart, promoCode = "SAVE10")

        val receipt = (result as BasketCalculationResult.Success).receipt
        assertEquals(0, Money.of("30.00").compareTo(receipt.subtotal))
        assertEquals(0, Money.of("3.00").compareTo(receipt.promoDiscount))
        assertEquals(0, Money.of("27.00").compareTo(receipt.discountedTotal))
    }

    @Test
    fun `a subtotal of fifty five with a ten percent code ships for five and totals fifty four and a half`() = runTest {
        val useCase = realUseCase(promoCode = promoCode(code = "SAVE10", percent = 10))
        // subtotal 55.00 → скидка 5.50 → discountedTotal 49.50 (ниже порога) → доставка 5.00 → итог 54.50
        val cart = Cart(items = listOf(cartItem(unitPrice = "55.00", quantity = 1)))

        val result = useCase(cart, promoCode = "SAVE10")

        val receipt = (result as BasketCalculationResult.Success).receipt
        assertEquals(0, Money.of("49.50").compareTo(receipt.discountedTotal))
        assertEquals(0, Money.of("5.00").compareTo(receipt.shippingCost))
        assertEquals(0, Money.of("54.50").compareTo(receipt.total))
    }

    @Test
    fun `the shipping policy receives the rounded discounted total`() = runTest {
        val promoCodeResolver = mockk<PromoCodeResolver>()
        val promoDiscountCalculator = mockk<PromoDiscountCalculator>()
        val shippingPolicy = mockk<ShippingPolicy>()
        coEvery { promoCodeResolver.resolve(any()) } returns PromoCodeStatus.Applied("SAVE", 10)
        // subtotal 50.005, скидка 0.010 → exactDiscountedTotal 49.995 → округление до 50.00
        coEvery { promoDiscountCalculator.discountOf(any(), any()) } returns Money.of("0.010")
        val totalSlot = slot<BigDecimal>()
        coEvery { shippingPolicy.shippingCostFor(capture(totalSlot), any()) } returns Money.ZERO
        val useCase = CalculateBasketUseCase(
            cartValidator = cartValidator,
            subtotalCalculator = subtotalCalculator,
            promoCodeResolver = promoCodeResolver,
            promoDiscountCalculator = promoDiscountCalculator,
            shippingPolicy = shippingPolicy,
        )
        val cart = Cart(items = listOf(cartItem(unitPrice = "50.005", quantity = 1)))

        val result = useCase(cart, promoCode = "SAVE")

        // В политику доставки уходит округлённый Discounted_Total 50.00, поэтому доставка бесплатна
        assertEquals(0, Money.of("50.00").compareTo(totalSlot.captured))
        val receipt = (result as BasketCalculationResult.Success).receipt
        assertEquals(0, Money.ZERO.compareTo(receipt.shippingCost))
    }

    @Test
    fun `a discount exceeding subtotal returns zero`() = runTest {
        val promoCodeResolver = mockk<PromoCodeResolver>()
        val promoDiscountCalculator = mockk<PromoDiscountCalculator>()
        val shippingPolicy = mockk<ShippingPolicy>()
        coEvery { promoCodeResolver.resolve(any()) } returns PromoCodeStatus.Applied("BIG", 100)
        // Скидка больше суммы позиций
        coEvery { promoDiscountCalculator.discountOf(any(), any()) } returns Money.of("100.00")
        coEvery { shippingPolicy.shippingCostFor(any(), any()) } returns Money.of("5.00")
        val useCase = CalculateBasketUseCase(
            cartValidator = cartValidator,
            subtotalCalculator = subtotalCalculator,
            promoCodeResolver = promoCodeResolver,
            promoDiscountCalculator = promoDiscountCalculator,
            shippingPolicy = shippingPolicy,
        )
        val cart = Cart(items = listOf(cartItem(unitPrice = "10.00", quantity = 1)))

        val result = useCase(cart, promoCode = "BIG")

        // Discounted_Total ограничен снизу нулём
        val receipt = (result as BasketCalculationResult.Success).receipt
        assertEquals(0, Money.ZERO.compareTo(receipt.discountedTotal))
    }

    @Test
    fun `an expired code keeps the subtotal and the shipping cost`() = runTest {
        // Просроченный промокод: истёк вчера относительно фиксированной «сегодня»
        val useCase = realUseCase(promoCode = promoCode(code = "OLD20", percent = 20, expiresOn = today.minusDays(1)))
        val cart = Cart(items = listOf(cartItem(unitPrice = "40.00", quantity = 1)))

        val result = useCase(cart, promoCode = "OLD20")

        val receipt = (result as BasketCalculationResult.Success).receipt
        // Скидки нет: Subtotal сохранён, доставка считается по полной сумме (40.00 < 50.00 → 5.00)
        assertEquals(0, Money.of("40.00").compareTo(receipt.subtotal))
        assertEquals(0, Money.ZERO.compareTo(receipt.promoDiscount))
        assertEquals(0, Money.of("40.00").compareTo(receipt.discountedTotal))
        assertEquals(0, Money.of("5.00").compareTo(receipt.shippingCost))
        assertTrue(receipt.promoCodeStatus is PromoCodeStatus.Expired, "Ожидался Expired, был ${receipt.promoCodeStatus}")
    }

    @Test
    fun `an unknown code keeps the subtotal and the shipping cost`() = runTest {
        // Код не найден в репозитории
        val useCase = realUseCase(promoCode = null)
        val cart = Cart(items = listOf(cartItem(unitPrice = "40.00", quantity = 1)))

        val result = useCase(cart, promoCode = "NOPE")

        val receipt = (result as BasketCalculationResult.Success).receipt
        // Скидки нет: Subtotal сохранён, доставка считается по полной сумме (40.00 < 50.00 → 5.00)
        assertEquals(0, Money.of("40.00").compareTo(receipt.subtotal))
        assertEquals(0, Money.ZERO.compareTo(receipt.promoDiscount))
        assertEquals(0, Money.of("40.00").compareTo(receipt.discountedTotal))
        assertEquals(0, Money.of("5.00").compareTo(receipt.shippingCost))
        assertTrue(receipt.promoCodeStatus is PromoCodeStatus.NotFound, "Ожидался NotFound, был ${receipt.promoCodeStatus}")
    }

    @Test
    fun `a missing code skips the promo code repository`() = runTest {
        val repository = mockk<PromoCodeRepository>()
        val useCase = realUseCase(repository = repository)
        val cart = Cart(items = listOf(cartItem(unitPrice = "40.00", quantity = 1)))

        useCase(cart, promoCode = null)

        // Отсутствующий код не приводит к обращению в репозиторий
        coVerify(exactly = 0) { repository.findByCode(any()) }
    }

    @Test
    fun `the receipt carries the resolved promo code status`() = runTest {
        val useCase = realUseCase(promoCode = promoCode(code = "SAVE10", percent = 10))
        val cart = Cart(items = listOf(cartItem(unitPrice = "40.00", quantity = 1)))

        val result = useCase(cart, promoCode = "SAVE10")

        val receipt = (result as BasketCalculationResult.Success).receipt
        // В чек попадает ровно тот статус, который вернул резолвер
        assertEquals(PromoCodeStatus.Applied("SAVE10", 10), receipt.promoCodeStatus)
    }

    // ----- Тесты формы Receipt (реальные компоненты) -----

    @Test
    fun `every money field of the receipt has money scale`() = runTest {
        val useCase = realUseCase(promoCode = promoCode(code = "SAVE10", percent = 10))
        val cart = Cart(items = listOf(cartItem(unitPrice = "55.00", quantity = 1)))

        val result = useCase(cart, promoCode = "SAVE10")

        val receipt = (result as BasketCalculationResult.Success).receipt
        // Каждое денежное поле округлено до money scale — отдельное утверждение на каждое
        assertEquals(Money.SCALE, receipt.subtotal.scale())
        assertEquals(Money.SCALE, receipt.promoDiscount.scale())
        assertEquals(Money.SCALE, receipt.discountedTotal.scale())
        assertEquals(Money.SCALE, receipt.shippingCost.scale())
        assertEquals(Money.SCALE, receipt.total.scale())
    }

    @Test
    fun `each money field is rounded HALF_UP once`() = runTest {
        val useCase = realUseCase(promoCode = promoCode(code = "SAVE33", percent = 33))
        // subtotal 10.01 → скидка 33% = 3.3033 → HALF_UP до 3.30; discountedTotal 6.7067 → 6.71
        val cart = Cart(items = listOf(cartItem(unitPrice = "10.01", quantity = 1)))

        val result = useCase(cart, promoCode = "SAVE33")

        val receipt = (result as BasketCalculationResult.Success).receipt
        assertEquals(0, Money.of("10.01").compareTo(receipt.subtotal))
        assertEquals(0, Money.of("3.30").compareTo(receipt.promoDiscount))
        assertEquals(0, Money.of("6.71").compareTo(receipt.discountedTotal))
    }

    @Test
    fun `total is the sum of rounded discounted total and shipping cost`() = runTest {
        val useCase = realUseCase(promoCode = promoCode(code = "SAVE10", percent = 10))
        // discountedTotal 49.50 + доставка 5.00 = 54.50
        val cart = Cart(items = listOf(cartItem(unitPrice = "55.00", quantity = 1)))

        val result = useCase(cart, promoCode = "SAVE10")

        val receipt = (result as BasketCalculationResult.Success).receipt
        val expectedTotal = receipt.discountedTotal.add(receipt.shippingCost)
        assertEquals(0, expectedTotal.compareTo(receipt.total))
    }

    // ----- Creation Method-хелперы -----

    /**
     * Собирает use case с реальными компонентами; внешние зависимости
     * (`PromoCodeRepository`, `ClockProvider`) подменяются.
     *
     * @param promoCode промокод, возвращаемый репозиторием по любому коду; `null` — код не найден
     * @param repository явный мок репозитория; по умолчанию отвечает [promoCode]
     */
    private fun realUseCase(
        promoCode: PromoCode? = null,
        repository: PromoCodeRepository = mockk {
            coEvery { findByCode(any()) } returns promoCode
        },
    ): CalculateBasketUseCase {
        val clock = ClockProvider { today }
        return CalculateBasketUseCase(
            cartValidator = cartValidator,
            subtotalCalculator = subtotalCalculator,
            promoCodeResolver = PromoCodeResolver(repository, clock),
            promoDiscountCalculator = PromoDiscountCalculator(),
            shippingPolicy = ThresholdShippingPolicy(),
        )
    }

    private fun cartItem(
        unitPrice: String,
        quantity: Int,
        productId: String = "p-$unitPrice-$quantity",
    ): CartItem = CartItem(
        productId = productId,
        name = "Product $productId",
        unitPrice = Money.of(unitPrice),
        quantity = quantity,
    )

    private fun promoCode(
        code: String = "SAVE10",
        percent: Int = 10,
        expiresOn: LocalDate = today.plusDays(1),
    ): PromoCode = PromoCode(code = code, percent = percent, expiresOn = expiresOn)
}
