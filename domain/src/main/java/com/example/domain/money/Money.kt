package com.example.domain.money

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Денежные константы и единственная точка округления в системе.
 *
 * Промежуточные величины расчёта хранятся с полной точностью `BigDecimal`;
 * округление до [SCALE] знаков режимом [ROUNDING_MODE] применяется один раз —
 * при сборке итогового чека.
 */
object Money {

    /** Число знаков после запятой в итоговых денежных значениях. */
    const val SCALE: Int = 2
    val ROUNDING_MODE: RoundingMode = RoundingMode.HALF_UP
    val ZERO: BigDecimal = BigDecimal.ZERO.setScale(SCALE)

    fun round(value: BigDecimal): BigDecimal = value.setScale(SCALE, ROUNDING_MODE)

    /** Создаёт денежное значение из строки, сохраняя точное десятичное представление. */
    fun of(value: String): BigDecimal = BigDecimal(value)
}
