package com.example.domain.calculation

import com.example.domain.model.Cart
import com.example.domain.model.CartItem
import com.example.domain.model.CartValidationError
import com.example.domain.money.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Валидация позиций корзины: количество должно быть не меньше 1, цена за единицу — не меньше 0.00.
 *
 * Валидатор возвращает список всех найденных ошибок, а не первую: пользователю нужно увидеть
 * все некорректные позиции сразу. Каждая ошибка несёт идентификатор товара, поэтому тесты
 * проверяют не только тип ошибки, но и её содержимое.
 *
 * Цена 0.00 допустима — запрещена только цена меньше нуля, поэтому ноль вынесен в отдельный тест.
 */
class CartValidatorTest {

    private val validator = CartValidator()

    @Test
    fun `zero quantity is reported as InvalidQuantity with the product id`() {
        val cart = Cart(items = listOf(cartItem(productId = "p-1", unitPrice = "10.00", quantity = 0)))

        val errors = validator.validate(cart)

        assertEquals(listOf(CartValidationError.InvalidQuantity(productId = "p-1", quantity = 0)), errors)
    }

    @Test
    fun `negative quantity is reported as InvalidQuantity`() {
        val cart = Cart(items = listOf(cartItem(productId = "p-2", unitPrice = "10.00", quantity = -3)))

        val errors = validator.validate(cart)

        assertEquals(listOf(CartValidationError.InvalidQuantity(productId = "p-2", quantity = -3)), errors)
    }

    @Test
    fun `negative unit price is reported as InvalidPrice with the product id`() {
        val cart = Cart(items = listOf(cartItem(productId = "p-3", unitPrice = "-0.01", quantity = 1)))

        val errors = validator.validate(cart)

        assertEquals(
            listOf(CartValidationError.InvalidPrice(productId = "p-3", unitPrice = Money.of("-0.01"))),
            errors,
        )
    }

    @Test
    fun `zero unit price is valid`() {
        val cart = Cart(items = listOf(cartItem(productId = "p-4", unitPrice = "0.00", quantity = 2)))

        assertEquals(emptyList<CartValidationError>(), validator.validate(cart))
    }

    @Test
    fun `a cart with several invalid items reports every error`() {
        val cart = Cart(
            items = listOf(
                cartItem(productId = "p-ok", unitPrice = "10.00", quantity = 1),
                cartItem(productId = "p-quantity", unitPrice = "10.00", quantity = 0),
                cartItem(productId = "p-price", unitPrice = "-5.00", quantity = 2),
            ),
        )

        val errors = validator.validate(cart)

        // Обе ошибки должны присутствовать: валидатор не останавливается на первой найденной
        assertEquals(2, errors.size)
        assertTrue(
            errors.contains(CartValidationError.InvalidQuantity(productId = "p-quantity", quantity = 0)),
            "Errors were $errors",
        )
        assertTrue(
            errors.contains(
                CartValidationError.InvalidPrice(productId = "p-price", unitPrice = Money.of("-5.00")),
            ),
            "Errors were $errors",
        )
    }

    @Test
    fun `a valid cart produces no errors`() {
        val cart = Cart(
            items = listOf(
                cartItem(productId = "p-5", unitPrice = "10.00", quantity = 1),
                cartItem(productId = "p-6", unitPrice = "5.50", quantity = 3),
            ),
        )

        assertEquals(emptyList<CartValidationError>(), validator.validate(cart))
    }

    @Test
    fun `an empty cart produces no errors`() {
        assertEquals(emptyList<CartValidationError>(), validator.validate(Cart.EMPTY))
    }

    private fun cartItem(
        productId: String,
        unitPrice: String,
        quantity: Int,
    ): CartItem = CartItem(
        productId = productId,
        name = "Product $productId",
        unitPrice = Money.of(unitPrice),
        quantity = quantity,
    )
}
