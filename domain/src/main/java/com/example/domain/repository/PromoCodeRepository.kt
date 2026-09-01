package com.example.domain.repository

import com.example.domain.model.PromoCode

/** Поиск промокода по коду; реализация — в модуле :data. */
interface PromoCodeRepository {
    suspend fun findByCode(code: String): PromoCode?
}
