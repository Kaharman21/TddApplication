package com.example.tddapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Связывает [BasketViewModel] со stateless [BasketScreen]:
 * хранит введённый промокод и пробрасывает события расчёта.
 */
@Composable
fun BasketRoute(
    modifier: Modifier = Modifier,
    viewModel: BasketViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var promoCode by remember { mutableStateOf("") }

    // Первичный расчёт корзины без промокода при открытии экрана
    LaunchedEffect(Unit) { viewModel.calculate(null) }

    BasketScreen(
        state = state,
        promoCode = promoCode,
        onPromoCodeChange = { promoCode = it },
        onApply = { viewModel.calculate(promoCode) },
        modifier = modifier,
    )
}
