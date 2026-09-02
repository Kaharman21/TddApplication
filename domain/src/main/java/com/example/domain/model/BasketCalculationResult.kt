package com.example.domain.model

/** Результат расчёта корзины: успешный чек либо ошибки валидации. */
sealed interface BasketCalculationResult {
    data class Success(val receipt: Receipt) : BasketCalculationResult

    /** Корзина некорректна: расчёт не выполнялся, перечислены все найденные ошибки. */
    data class Failure(val errors: List<CartValidationError>) : BasketCalculationResult
}
