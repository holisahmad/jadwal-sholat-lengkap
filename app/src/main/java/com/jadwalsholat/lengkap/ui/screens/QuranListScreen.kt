package com.jadwalsholat.lengkap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalsholat.lengkap.ads.AdmobBanner
import com.jadwalsholat.lengkap.data.PreferenceManager
import com.jadwalsholat.lengkap.data.QuranRepository
import com.jadwalsholat.lengkap.data.Surah
import com.jadwalsholat.lengkap.ui.theme.NurBg
import com.jadwalsholat.lengkap.ui.theme.NurBorder
import com.jadwalsholat.lengkap.ui.theme.NurFaint
import com.jadwalsholat.lengkap.ui.theme.NurFg
import com.jadwalsholat.lengkap.ui.theme.NurGold
import com.jadwalsholat.lengkap.ui.theme.NurMuted
import com.jadwalsholat.lengkap.ui.theme.NurPrimary
import com.jadwalsholat.lengkap.ui.theme.NurPrimaryFg
import com.jadwalsholat.lengkap.ui.theme.NurRaised
import com.jadwalsholat.lengkap.ui.theme.NurSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranListScreen(
    preferenceManager: PreferenceManager,
    onSurahClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var surahs by remember { mutableStateOf<List<Surah>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showBookmarksOnly by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val list = QuranRepository.getSurahs(context)
        surahs = list
        isLoading = false
    }

    // Reactive bookmark state — updated on every toggle so icon recomposes immediately
    var bookmarkedIds by remember { mutableStateOf(preferenceManager.getBookmarks()) }

    val filteredSurahs = remember(surahs, searchQuery, showBookmarksOnly, bookmarkedIds) {
        surahs.filter { surah ->
            val matchesSearch = surah.englishName.contains(searchQuery, ignoreCase = true) ||
                    surah.englishNameTranslation.contains(searchQuery, ignoreCase = true) ||
                    surah.number.toString() == searchQuery.trim()

            val matchesBookmark = !showBookmarksOnly || bookmarkedIds.contains(surah.number.toString())

            matchesSearch && matchesBookmark
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
                text = "KITAB SUCI",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = NurMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Al-Qur'an",
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NurFg
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "114 Surah · Terjemahan Bahasa Indonesia",
                fontSize = 13.sp,
                color = NurMuted
            )
        }

        // Last Read Banner
        if (preferenceManager.lastSurah > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(1.dp, NurBorder, RoundedCornerShape(12.dp))
                    .clickable { onSurahClick(preferenceManager.lastSurah) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NurSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NurRaised),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = NurPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Terakhir Dibaca",
                                fontSize = 11.sp,
                                color = NurMuted
                            )
                            Text(
                                text = "${preferenceManager.lastSurahName} · ayat ${preferenceManager.lastAyah}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NurFg
                            )
                        }
                    }

                    Text(
                        text = "Lanjut ›",
                        color = NurPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Search Bar & Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Cari surah...", fontSize = 13.sp, color = NurMuted) },
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

            Spacer(modifier = Modifier.width(8.dp))

            FilterChip(
                selected = showBookmarksOnly,
                onClick = { showBookmarksOnly = !showBookmarksOnly },
                label = { Text("Bookmark", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = NurSurface,
                    labelColor = NurMuted,
                    selectedContainerColor = NurRaised,
                    selectedLabelColor = NurPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = showBookmarksOnly,
                    borderColor = NurBorder,
                    selectedBorderColor = NurPrimary
                )
            )
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
                items(filteredSurahs) { surah ->
                    val isBookmarked = preferenceManager.isBookmarked(surah.number)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, NurBorder, RoundedCornerShape(12.dp))
                            .clickable { onSurahClick(surah.number) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NurSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(NurRaised),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = surah.number.toString(),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = NurPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = surah.englishName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NurFg
                                    )
                                    Text(
                                        text = "${surah.typeIndonesian} · ${surah.totalAyahs} ayat",
                                        fontSize = 11.sp,
                                        color = NurMuted
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = surah.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = NurFg
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = {
                                        preferenceManager.toggleBookmark(surah.number)
                                        // Trigger recompose by refreshing state
                                        bookmarkedIds = preferenceManager.getBookmarks()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (isBookmarked) NurGold else NurFaint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
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
