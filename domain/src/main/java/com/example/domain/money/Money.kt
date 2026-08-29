package com.example.domain.money

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Денежные константы и единственная точка округления в системе.
 *
 * Промежуточные величины расчёта хранятся с полной точностью `BigDecimal`;
 * округление до [SCALE] знаков режимом [ROUNDING_MODE] применяется один раз —
 * при сборке итогового чека. Требования 1.4, 6.6.
 */
object Money {

    /** Число знаков после запятой в итоговых денежных значениях. */
    const val SCALE: Int = 2

    /** Режим округления итоговых значений — HALF_UP. */
    val ROUNDING_MODE: RoundingMode = RoundingMode.HALF_UP

    /** Ноль с денежным scale. */
    val ZERO: BigDecimal = BigDecimal.ZERO.setScale(SCALE)

    /**
     * Округляет значение до [SCALE] знаков режимом [ROUNDING_MODE].
     * Другие места кода округление применять не должны. Требования 6.2, 6.6.
     */
    fun round(value: BigDecimal): BigDecimal = value.setScale(SCALE, ROUNDING_MODE)

    /** Создаёт денежное значение из строки, сохраняя точное десятичное представление. */
    fun of(value: String): BigDecimal = BigDecimal(value)
}
