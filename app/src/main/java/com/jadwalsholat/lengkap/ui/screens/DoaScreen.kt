package com.jadwalsholat.lengkap.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalsholat.lengkap.ads.AdmobBanner
import com.jadwalsholat.lengkap.data.DoaItem
import com.jadwalsholat.lengkap.data.DoaRepository
import com.jadwalsholat.lengkap.data.PreferenceManager
import com.jadwalsholat.lengkap.ui.theme.getArabicFontFamily
import com.jadwalsholat.lengkap.ui.theme.NurBg
import com.jadwalsholat.lengkap.ui.theme.NurBorder
import com.jadwalsholat.lengkap.ui.theme.NurFaint
import com.jadwalsholat.lengkap.ui.theme.NurFg
import com.jadwalsholat.lengkap.ui.theme.NurMuted
import com.jadwalsholat.lengkap.ui.theme.NurPrimary
import com.jadwalsholat.lengkap.ui.theme.NurRaised
import com.jadwalsholat.lengkap.ui.theme.NurSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoaScreen(
    preferenceManager: PreferenceManager? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pref = remember(preferenceManager) { preferenceManager ?: PreferenceManager(context) }
    val arabicFont = remember { getArabicFontFamily(pref.arabicFont) }
    var doas by remember { mutableStateOf<List<DoaItem>>(emptyList()) }
    var categories by remember { mutableStateOf<List<String>>(listOf("Semua")) }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val loadedDoas = DoaRepository.getDoas(context)
        val loadedCategories = DoaRepository.getCategories(context)
        doas = loadedDoas
        categories = loadedCategories
        isLoading = false
    }

    val filteredDoas = remember(doas, selectedCategory, searchQuery) {
        doas.filter { doa ->
            val matchesCategory = selectedCategory == "Semua" || doa.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = doa.title.contains(searchQuery, ignoreCase = true) ||
                    doa.arti.contains(searchQuery, ignoreCase = true) ||
                    doa.latin.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NurBg)
    ) {
        // Minimalist Editorial Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "DZIKIR & DOA",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = NurMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kumpulan Doa",
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NurFg
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Doa harian, sholat, makan & perlindungan",
                fontSize = 13.sp,
                color = NurMuted
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("Cari doa...", fontSize = 13.sp, color = NurMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari",
                    tint = NurMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NurFg,
                unfocusedTextColor = NurFg,
                focusedContainerColor = NurSurface,
                unfocusedContainerColor = NurSurface,
                focusedBorderColor = NurPrimary,
                unfocusedBorderColor = NurBorder
            )
        )

        // Category Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = NurSurface,
                        labelColor = NurMuted,
                        selectedContainerColor = NurRaised,
                        selectedLabelColor = NurPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = NurBorder,
                        selectedBorderColor = NurPrimary
                    )
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NurPrimary, modifier = Modifier.size(32.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredDoas) { doa ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .border(1.dp, NurBorder, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NurSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Title & Category badge + actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doa.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NurFg
                                    )
                                    Text(
                                        text = doa.category,
                                        fontSize = 11.sp,
                                        color = NurMuted
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText(
                                                doa.title,
                                                "${doa.title}\n\n${doa.arab}\n\nLatin: ${doa.latin}\n\nArtinya: \"${doa.arti}\""
                                            )
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Doa berhasil disalin!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Salin",
                                            tint = NurMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "${doa.title}\n\n${doa.arab}\n\nLatin: ${doa.latin}\n\nArtinya: \"${doa.arti}\""
                                                )
                                                type = "text/plain"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Doa")
                                            context.startActivity(shareIntent)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Bagikan",
                                            tint = NurMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Arabic text (Scheherazade New)
                            Text(
                                text = doa.arab,
                                fontSize = 20.sp,
                                lineHeight = 42.sp,
                                fontFamily = arabicFont,
                                textAlign = TextAlign.End,
                                color = NurFg,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Latin transliteration
                            Text(
                                text = doa.latin,
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color = NurPrimary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(NurBorder.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Meaning
                            Text(
                                text = "\"${doa.arti}\"",
                                fontSize = 12.sp,
                                color = NurMuted,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        AdmobBanner(modifier = Modifier.background(NurSurface))
    }
}
