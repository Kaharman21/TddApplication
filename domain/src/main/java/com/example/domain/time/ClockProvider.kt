package com.example.domain.time

import java.time.LocalDate

/** Текущая дата; подменяется в тестах лямбдой, чтоб через год-два тест не поломался */
fun interface ClockProvider {
    fun today(): LocalDate
}
