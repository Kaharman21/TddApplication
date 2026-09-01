package com.example.domain.time

import java.time.LocalDate

/** Текущая дата; подменяется в тестах лямбдой. */
fun interface ClockProvider {
    fun today(): LocalDate
}
