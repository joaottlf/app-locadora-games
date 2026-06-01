package com.example.trabalho.data

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

// Molde de resposta do nosso recálculo
data class RecalculationResult(
    val status: String, // "ATRASADO", "ADIANTADO", "NO_PRAZO"
    val actualDays: Int,
    val newTotal: Double,
    val difference: Double // Pode ser a multa ou o valor que o cliente tem de crédito
)

object RentCalculator {

    fun calculateDaysBetween(startDate: Date, endDate: Date): Int {
        val startCal = Calendar.getInstance().apply {
            time = startDate
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            time = endDate
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val diffInMillis = endCal.timeInMillis - startCal.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
    }

    fun calculateTotalRentPrice(dailyPrice: Double, days: Int): Double {
        val basePrice = dailyPrice * days
        val discountPercentage = when {
            days >= 180 -> 0.30
            days >= 150 -> 0.25
            days >= 120 -> 0.20
            days >= 90  -> 0.15
            days >= 60  -> 0.10
            days >= 30  -> 0.05
            else        -> 0.00
        }
        return basePrice * (1.0 - discountPercentage)
    }

    fun calculateReturnDate(days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }

    // --- LÓGICA DO BLOCO 3: RECÁLCULO E MULTAS ---
    fun calculateDaysLate(returnDate: Date): Int {
        return calculateDaysBetween(returnDate, Date())
    }

    fun recalculateReturnPrice(
        originalPricePaid: Double,
        baseDailyPrice: Double,
        rentDaysContracted: Int,
        returnDateLimit: Date
    ): RecalculationResult {
        val extraDays = calculateDaysLate(returnDateLimit)

        return when {
            extraDays > 0 -> {
                // ATRASADO: Multa cobrando o dobro da diária por dia extra!
                val lateFee = (baseDailyPrice * 2.0) * extraDays
                val newTotal = originalPricePaid + lateFee
                RecalculationResult("ATRASADO", rentDaysContracted + extraDays, newTotal, lateFee)
            }
            extraDays < 0 -> {
                // ADIANTADO: Recalcula o valor removendo os descontos indevidos
                val actualDays = maxOf(1, rentDaysContracted + extraDays) // Cliente ficou no mínimo 1 dia
                val newTotal = calculateTotalRentPrice(baseDailyPrice, actualDays)
                val difference = originalPricePaid - newTotal // Dinheiro que o cliente pagou a mais
                RecalculationResult("ADIANTADO", actualDays, newTotal, difference)
            }
            else -> {
                // NO PRAZO EXATO
                RecalculationResult("NO_PRAZO", rentDaysContracted, originalPricePaid, 0.0)
            }
        }
    }
}