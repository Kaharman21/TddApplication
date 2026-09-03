package com.example.tddapplication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.domain.model.CartValidationError
import com.example.domain.model.PromoCodeStatus
import com.example.domain.model.Receipt
import com.example.domain.money.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

/**
 * Instrumented Compose UI-тесты экрана корзины [BasketScreen].
 *
 * Экран stateless: получает [BasketUiState], текущий промокод и колбэки,
 * а тесты проверяют отображение каждого состояния и проброс событий ввода.
 * Поиск элементов идёт по заранее согласованным testTag.
 */
class BasketScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `loading state shows a progress indicator`() {
        composeRule.setContent {
            BasketScreen(
                state = BasketUiState.Loading,
                promoCode = "",
                onPromoCodeChange = {},
                onApply = {},
            )
        }

        composeRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
    }

    @Test
    fun `success state shows the receipt total`() {
        val receipt = receiptWithTotal(Money.of("54.50"))

        composeRule.setContent {
            BasketScreen(
                state = BasketUiState.Success(receipt),
                promoCode = "",
                onPromoCodeChange = {},
                onApply = {},
            )
        }

        composeRule.onNodeWithTag("receiptTotal")
            .assertIsDisplayed()
            .assertTextContains("54.50", substring = true)
    }

    @Test
    fun `error state shows a validation message`() {
        val errors = listOf(CartValidationError.InvalidQuantity(productId = "p1", quantity = 0))

        composeRule.setContent {
            BasketScreen(
                state = BasketUiState.Error(errors),
                promoCode = "",
                onPromoCodeChange = {},
                onApply = {},
            )
        }

        composeRule.onNodeWithTag("errorMessage").assertIsDisplayed()
    }

    @Test
    fun `typing in the promo field triggers the change callback`() {
        var captured = ""

        composeRule.setContent {
            BasketScreen(
                state = BasketUiState.Loading,
                promoCode = "",
                onPromoCodeChange = { captured = it },
                onApply = {},
            )
        }

        composeRule.onNodeWithTag("promoField").performTextInput("SAVE10")

        assertEquals("SAVE10", captured)
    }

    @Test
    fun `clicking apply triggers the apply callback`() {
        var clicked = false

        composeRule.setContent {
            BasketScreen(
                state = BasketUiState.Success(Receipt.empty()),
                promoCode = "SAVE10",
                onPromoCodeChange = {},
                onApply = { clicked = true },
            )
        }

        composeRule.onNodeWithTag("applyButton").performClick()

        assertTrue(clicked)
    }

    /** Собирает чек с заданным [total]; остальные суммы нулевые, промокод не применён. */
    private fun receiptWithTotal(total: BigDecimal): Receipt = Receipt(
        subtotal = Money.ZERO,
        promoDiscount = Money.ZERO,
        discountedTotal = Money.ZERO,
        shippingCost = Money.ZERO,
        total = total,
        promoCodeStatus = PromoCodeStatus.NotApplied,
    )
}
