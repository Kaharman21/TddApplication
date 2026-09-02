package com.example.tddapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BasketCalculationResult
import com.example.domain.repository.CartRepository
import com.example.domain.usecase.CalculateBasketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel экрана корзины: запускает расчёт и публикует состояние. */
@HiltViewModel
class BasketViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val calculateBasket: CalculateBasketUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BasketUiState>(BasketUiState.Loading)
    val uiState: StateFlow<BasketUiState> = _uiState.asStateFlow()

    fun calculate(promoCode: String?) {
        viewModelScope.launch {
            val cart = cartRepository.getCart()
            _uiState.value = when (val result = calculateBasket(cart, promoCode)) {
                is BasketCalculationResult.Success -> BasketUiState.Success(result.receipt)
                is BasketCalculationResult.Failure -> BasketUiState.Error(result.errors)
            }
        }
    }
}
