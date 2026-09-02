package com.example.tddapplication

import com.example.domain.model.CartValidationError
import com.example.domain.model.Receipt

/** Состояние экрана корзины. */
sealed interface BasketUiState {
    data object Loading : BasketUiState
    data class Success(val receipt: Receipt) : BasketUiState
    data class Error(val errors: List<CartValidationError>) : BasketUiState
}
