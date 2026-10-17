package com.jadwalsholat.lengkap.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.jadwalsholat.lengkap.data.PreferenceManager
import com.jadwalsholat.lengkap.data.QuranRepository
import com.jadwalsholat.lengkap.data.Surah
import com.jadwalsholat.lengkap.ui.theme.getArabicFontFamily
import com.jadwalsholat.lengkap.ui.theme.NurBg
import com.jadwalsholat.lengkap.ui.theme.NurBorder
import com.jadwalsholat.lengkap.ui.theme.NurFaint
import com.jadwalsholat.lengkap.ui.theme.NurFg
import com.jadwalsholat.lengkap.ui.theme.NurMuted
import com.jadwalsholat.lengkap.ui.theme.NurPrimary
import com.jadwalsholat.lengkap.ui.theme.NurPrimaryFg
import com.jadwalsholat.lengkap.ui.theme.NurRaised
import com.jadwalsholat.lengkap.ui.theme.NurSurface

/**
 * 100% BEBAS IKLAN (ADS-FREE) pada halaman baca Al-Qur'an.
 *
 * Mode tampilan:
 * - Default: Arab + Terjemahan Indonesia
 * - Mode Mengaji (arabOnly = true): Arab saja, tanpa terjemahan — cocok untuk mengaji tanpa banyak scroll
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranDetailScreen(
    surahNumber: Int,
    preferenceManager: PreferenceManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var surah by remember { mutableStateOf<Surah?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Mode mengaji: persisten lintas sesi
    var arabOnly by remember { mutableStateOf(preferenceManager.quranArabOnly) }
    val arabicFont = remember { getArabicFontFamily(preferenceManager.arabicFont) }

    // Keep screen on while reading Quran (layar tetap menyala, tidak standby)
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(surahNumber) {
        val loaded = QuranRepository.getSurahByNumber(context, surahNumber)
        surah = loaded
        if (loaded != null) {
            preferenceManager.lastSurah = loaded.number
            preferenceManager.lastSurahName = loaded.englishName
        }
        isLoading = false
    }

    Scaffold(
        containerColor = NurBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = surah?.englishName ?: "Memuat...",
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NurFg
                        )
                        Text(
                            text = "${surah?.typeIndonesian ?: ""} · ${surah?.totalAyahs ?: 0} ayat",
                            fontSize = 11.sp,
                            color = NurMuted
                        )
                    }
                },
                actions = {
                    // Tombol toggle mode tampilan
                    IconButton(
                        onClick = {
                            // Simpan posisi ayat yang sedang terlihat sebelum toggle
                            val currentIndex = listState.firstVisibleItemIndex
                            arabOnly = !arabOnly
                            preferenceManager.quranArabOnly = arabOnly
                            // Scroll kembali ke ayat yang sama setelah layout berubah
                            coroutineScope.launch {
                                listState.scrollToItem(currentIndex)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = if (arabOnly) "Tampilkan terjemahan" else "Sembunyikan terjemahan",
                            tint = if (arabOnly) NurFg else NurPrimary
                        )
                    }
                    // Nama Arab surah
                    surah?.let {
                        Text(
                            text = it.name,
                            fontSize = 20.sp,
                            fontFamily = arabicFont,
                            fontWeight = FontWeight.Normal,
                            color = NurFg,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = NurFg
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NurSurface)
            )
        }
    ) { innerPadding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NurPrimary, modifier = Modifier.size(32.dp))
            }
        } else if (surah == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Surah tidak ditemukan.", color = NurMuted)
            }
        } else {
            val currentSurah = surah!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Mode indicator pill
                AnimatedVisibility(visible = arabOnly) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = NurRaised,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Mode Mengaji — terjemahan disembunyikan",
                                fontSize = 11.sp,
                                color = NurPrimary,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.3.sp
                            )
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = NurPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Bismillah banner (kecuali Surah At-Taubah no 9 & Al-Fatihah no 1)
                    if (currentSurah.number != 9 && currentSurah.number != 1) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, NurBorder, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = NurSurface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                        fontSize = 24.sp,
                                        fontFamily = arabicFont,
                                        fontWeight = FontWeight.Bold,
                                        color = NurFg
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    items(currentSurah.ayahs) { ayah ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .border(1.dp, NurBorder, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = NurSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                                // Ayah number + action buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(NurRaised),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ayah.number.toString(),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = NurPrimary
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                preferenceManager.lastSurah = currentSurah.number
                                                preferenceManager.lastAyah = ayah.number
                                                preferenceManager.lastSurahName = currentSurah.englishName
                                                Toast.makeText(
                                                    context,
                                                    "Ditandai: ${currentSurah.englishName} Ayat ${ayah.number}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.BookmarkAdd,
                                                contentDescription = "Tandai Terakhir Dibaca",
                                                tint = NurPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText(
                                                    "Ayat Al-Qur'an",
                                                    "${ayah.arab}\n\n\"${ayah.id}\" (QS. ${currentSurah.englishName}: ${ayah.number})"
                                                )
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Ayat berhasil disalin!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Salin Ayat",
                                                tint = NurMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Arabic Text — selalu tampil (Scheherazade New)
                                Text(
                                    text = ayah.arab,
                                    fontSize = 24.sp,
                                    lineHeight = 50.sp,
                                    fontFamily = arabicFont,
                                    textAlign = TextAlign.End,
                                    color = NurFg,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Terjemahan — hanya tampil saat arabOnly = false
                                AnimatedVisibility(
                                    visible = !arabOnly,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(NurBorder.copy(alpha = 0.5f))
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = ayah.id,
                                            fontSize = 13.sp,
                                            color = NurMuted,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
