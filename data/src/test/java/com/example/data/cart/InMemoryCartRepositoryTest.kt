package com.example.data.cart

import com.example.domain.model.Cart
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * Демонстрационная корзина для экрана.
 *
 * Репозиторий отдаёт заранее заданный набор позиций, поэтому проверяется не конкретное
 * содержимое, а инварианты демо-данных: корзина непустая и каждая позиция корректна
 * (количество не меньше единицы, цена за единицу неотрицательна). Так тест не привязан к
 * точным товарам и не ломается при их замене, но гарантирует, что демо-корзина проходит
 * валидацию расчёта. Репозиторий создаётся конструктором по умолчанию.
 */
class InMemoryCartRepositoryTest {

    private val repository = InMemoryCartRepository()

    @Test
    fun `the demo cart is not empty`() = runTest {
        val cart: Cart = repository.getCart()

        assertTrue(cart.items.isNotEmpty())
    }

    @Test
    fun `every demo cart item has a valid quantity and unit price`() = runTest {
        val cart: Cart = repository.getCart()

        cart.items.forEach { item ->
            assertTrue(item.quantity >= 1)
            assertTrue(item.unitPrice >= BigDecimal.ZERO)
        }
    }
}
