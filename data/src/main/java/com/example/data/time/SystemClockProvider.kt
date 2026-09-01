package com.example.data.time

import com.example.domain.time.ClockProvider
import java.time.LocalDate

/** Системный провайдер текущей даты. */
class SystemClockProvider : ClockProvider {
    override fun today(): LocalDate = LocalDate.now()
}
