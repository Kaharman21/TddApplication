package com.example.tddapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.CartValidationError
import com.example.domain.model.PromoCodeStatus

/** Stateless экран корзины: рендерит состояние и пробрасывает ввод промокода. */
@Composable
fun BasketScreen(
    state: BasketUiState,
    promoCode: String,
    onPromoCodeChange: (String) -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = promoCode,
            onValueChange = onPromoCodeChange,
            label = { Text("Промокод") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("promoField"),
        )
        Button(
            onClick = onApply,
            modifier = Modifier.testTag("applyButton"),
        ) {
            Text("Применить")
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (state) {
            BasketUiState.Loading -> {
                CircularProgressIndicator(Modifier.testTag("loadingIndicator"))
            }

            is BasketUiState.Success -> {
                val receipt = state.receipt
                Text(
                    text = "Итого: ${receipt.total.toPlainString()}",
                    modifier = Modifier.testTag("receiptTotal"),
                )
                Text(
                    text = promoStatusText(receipt.promoCodeStatus),
                    modifier = Modifier.testTag("promoStatus"),
                )
            }

            is BasketUiState.Error -> {
                Text(
                    text = errorText(state.errors),
                    modifier = Modifier.testTag("errorMessage"),
                )
            }
        }
    }
}

/** Человекочитаемый статус промокода. */
private fun promoStatusText(status: PromoCodeStatus): String = when (status) {
    PromoCodeStatus.NotApplied -> ""
    is PromoCodeStatus.Applied -> "Промокод применён"
    is PromoCodeStatus.NotFound -> "Промокод не найден"
    is PromoCodeStatus.Expired -> "Срок промокода истёк"
}

/** Сообщение об ошибках валидации корзины. */
private fun errorText(errors: List<CartValidationError>): String {
    val ids = errors.joinToString(", ") { it.productId }
    return "Ошибка: ${errors.size} позиций некорректны ($ids)"
}
