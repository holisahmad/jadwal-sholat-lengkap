package com.jadwalsholat.lengkap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalsholat.lengkap.ads.AdmobBanner
import com.jadwalsholat.lengkap.data.PreferenceManager
import com.jadwalsholat.lengkap.data.ZakatCalculator
import com.jadwalsholat.lengkap.ui.theme.NurBg
import com.jadwalsholat.lengkap.ui.theme.NurBorder
import com.jadwalsholat.lengkap.ui.theme.NurFaint
import com.jadwalsholat.lengkap.ui.theme.NurFg
import com.jadwalsholat.lengkap.ui.theme.NurMuted
import com.jadwalsholat.lengkap.ui.theme.NurPrimary
import com.jadwalsholat.lengkap.ui.theme.NurSurface

@Composable
fun ZakatScreen(
    preferenceManager: PreferenceManager,
    modifier: Modifier = Modifier
) {
    var goldPriceInput by remember { mutableStateOf(preferenceManager.goldPrice.toLong().toString()) }
    var cashInput by remember { mutableStateOf("") }
    var goldGramInput by remember { mutableStateOf("") }
    var silverInput by remember { mutableStateOf("") }
    var tradeInput by remember { mutableStateOf("") }

    val goldPrice = goldPriceInput.toDoubleOrNull() ?: 1450000.0
    val cash = cashInput.toDoubleOrNull() ?: 0.0
    val goldGrams = goldGramInput.toDoubleOrNull() ?: 0.0
    val silver = silverInput.toDoubleOrNull() ?: 0.0
    val trade = tradeInput.toDoubleOrNull() ?: 0.0

    val result = remember(goldPrice, cash, goldGrams, silver, trade) {
        ZakatCalculator.calculate(
            goldPricePerGram = goldPrice,
            cashSavings = cash,
            goldGrams = goldGrams,
            silverValue = silver,
            tradeAssets = trade
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NurBg)
    ) {
        // Minimalist Editorial Header (Matching Web exactly)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "HISAB",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = NurMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Zakat mal",
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NurFg
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kalkulator sederhana untuk tabungan, emas, dan dagangan.",
                fontSize = 13.sp,
                color = NurMuted
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Perkiraan Zakat Card (Exact Web layout)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NurBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NurSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "PERKIRAAN ZAKAT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.6.sp,
                            color = NurMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = ZakatCalculator.formatRupiah(result.zakatDue),
                            fontFamily = FontFamily.Serif,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = NurFg,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = result.statusMessage,
                            fontSize = 13.sp,
                            color = NurMuted
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Nisab saat ini ${ZakatCalculator.formatRupiah(result.nisabAmount)}",
                            fontSize = 11.sp,
                            color = NurFaint
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Input Fields
            item {
                ZakatInputField(
                    label = "Harga emas per gram",
                    value = goldPriceInput,
                    onValueChange = {
                        goldPriceInput = it
                        it.toDoubleOrNull()?.let { p -> preferenceManager.goldPrice = p }
                    },
                    hint = "Perbarui sesuai harga toko hari ini"
                )
            }

            item {
                ZakatInputField(
                    label = "Uang & tabungan",
                    value = cashInput,
                    onValueChange = { cashInput = it }
                )
            }

            item {
                ZakatInputField(
                    label = "Emas (gram)",
                    value = goldGramInput,
                    onValueChange = { goldGramInput = it },
                    suffix = "g"
                )
            }

            item {
                ZakatInputField(
                    label = "Perak (nilai rupiah)",
                    value = silverInput,
                    onValueChange = { silverInput = it }
                )
            }

            item {
                ZakatInputField(
                    label = "Harta dagangan",
                    value = tradeInput,
                    onValueChange = { tradeInput = it }
                )
            }

            item {
                Text(
                    text = "Ini alat bantu, bukan fatwa. Konsultasikan nisab, haul, dan jenis harta kepada lembaga amil atau ustaz yang Anda percayai.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = NurFaint,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        AdmobBanner(modifier = Modifier.background(NurSurface))
    }
}

@Composable
private fun ZakatInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String? = null,
    suffix: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = NurFg
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("0", fontSize = 14.sp, color = NurMuted) },
            suffix = suffix?.let { { Text(it, fontSize = 12.sp, color = NurMuted) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NurFg,
                unfocusedTextColor = NurFg,
                focusedContainerColor = NurSurface,
                unfocusedContainerColor = NurSurface,
                focusedBorderColor = NurPrimary,
                unfocusedBorderColor = NurBorder
            )
        )
        if (hint != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = hint, fontSize = 11.sp, color = NurFaint)
        }
    }
}
