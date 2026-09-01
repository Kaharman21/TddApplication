package com.example.domain.repository

import com.example.domain.model.Cart

/** Получение корзины; реализация — в модуле :data. */
interface CartRepository {
    suspend fun getCart(): Cart
}
