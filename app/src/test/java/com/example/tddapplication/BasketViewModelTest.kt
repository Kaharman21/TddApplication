package com.example.tddapplication

import com.example.domain.model.BasketCalculationResult
import com.example.domain.model.Cart
import com.example.domain.model.CartItem
import com.example.domain.model.CartValidationError
import com.example.domain.model.Receipt
import com.example.domain.money.Money
import com.example.domain.repository.CartRepository
import com.example.domain.usecase.CalculateBasketUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Red-фаза TDD: падающие example-based тесты для ещё не существующих
 * [BasketViewModel] и [BasketUiState].
 *
 * viewModelScope работает на Dispatchers.Main — в тестах подменяется тестовым
 * диспетчером через Dispatchers.setMain/resetMain. Текущее значение состояния
 * читается напрямую через uiState.value (StateFlow хранит последнее значение).
 */
class BasketViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `the initial state is loading`() {
        // Сразу после создания состояние — Loading, расчёт ещё не запускался
        val cartRepository = mockk<CartRepository>(relaxed = true)
        val calculateBasket = mockk<CalculateBasketUseCase>(relaxed = true)

        val viewModel = BasketViewModel(cartRepository, calculateBasket)

        assertEquals(BasketUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `a valid cart exposes a success state with the receipt`() = runTest(dispatcher) {
        // Валидная корзина: use case возвращает успешный чек
        val cart = Cart(listOf(cartItem(unitPrice = "10.00", quantity = 2, productId = "p1")))
        val receipt = Receipt.empty()
        val cartRepository = mockk<CartRepository>()
        val calculateBasket = mockk<CalculateBasketUseCase>()
        coEvery { cartRepository.getCart() } returns cart
        coEvery { calculateBasket(cart, "SAVE10") } returns BasketCalculationResult.Success(receipt)

        val viewModel = BasketViewModel(cartRepository, calculateBasket)
        viewModel.calculate("SAVE10")
        advanceUntilIdle()

        assertEquals(BasketUiState.Success(receipt), viewModel.uiState.value)
    }

    @Test
    fun `an invalid cart exposes an error state with the validation errors`() = runTest(dispatcher) {
        // Некорректная корзина: use case возвращает ошибки валидации
        val cart = Cart(listOf(cartItem(unitPrice = "10.00", quantity = 0, productId = "p1")))
        val errors = listOf<CartValidationError>(
            CartValidationError.InvalidQuantity(productId = "p1", quantity = 0),
        )
        val cartRepository = mockk<CartRepository>()
        val calculateBasket = mockk<CalculateBasketUseCase>()
        coEvery { cartRepository.getCart() } returns cart
        coEvery { calculateBasket(cart, null) } returns BasketCalculationResult.Failure(errors)

        val viewModel = BasketViewModel(cartRepository, calculateBasket)
        viewModel.calculate(null)
        advanceUntilIdle()

        assertEquals(BasketUiState.Error(errors), viewModel.uiState.value)
    }

    /** Создаёт позицию корзины с денежной ценой из строки. */
    private fun cartItem(unitPrice: String, quantity: Int, productId: String): CartItem = CartItem(
        productId = productId,
        name = productId,
        unitPrice = Money.of(unitPrice),
        quantity = quantity,
    )
}
