package com.jadwalsholat.lengkap.data

import java.text.NumberFormat
import java.util.Locale

data class ZakatCalculationResult(
    val totalWealth: Double,
    val nisabAmount: Double,
    val isEligible: Boolean,
    val zakatDue: Double,
    val statusMessage: String
)

object ZakatCalculator {
    const val NISAB_GOLD_GRAM = 85.0
    const val ZAKAT_RATE = 0.025 // 2.5%

    fun calculate(
        goldPricePerGram: Double,
        cashSavings: Double,
        goldGrams: Double,
        silverValue: Double,
        tradeAssets: Double
    ): ZakatCalculationResult {
        val goldValue = goldGrams * goldPricePerGram
        val totalWealth = cashSavings + goldValue + silverValue + tradeAssets
        val nisab = goldPricePerGram * NISAB_GOLD_GRAM
        val isEligible = totalWealth >= nisab && totalWealth > 0

        val due = if (isEligible) totalWealth * ZAKAT_RATE else 0.0

        val status = when {
            totalWealth <= 0 -> "Silakan masukkan nilai harta yang telah mencapai haul (1 tahun)."
            !isEligible -> "Belum mencapai nisab (setara 85 gram emas atau ${formatRupiah(nisab)})."
            else -> "Wajib mengeluarkan zakat sebesar 2,5% (${formatRupiah(due)})."
        }

        return ZakatCalculationResult(
            totalWealth = totalWealth,
            nisabAmount = nisab,
            isEligible = isEligible,
            zakatDue = due,
            statusMessage = status
        )
    }

    fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount.coerceAtLeast(0.0))
    }
}
