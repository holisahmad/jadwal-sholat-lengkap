package com.jadwalsholat.lengkap.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

data class QuranResponse(
    @SerializedName("surahs") val surahs: List<Surah>
)

data class Surah(
    @SerializedName("number") val number: Int,
    @SerializedName("name") val name: String,
    @SerializedName("englishName") val englishName: String,
    @SerializedName("englishNameTranslation") val englishNameTranslation: String,
    @SerializedName("revelationType") val revelationType: String,
    @SerializedName("ayahs") val ayahs: List<Ayah>
) {
    val typeIndonesian: String
        get() = if (revelationType.equals("Meccan", ignoreCase = true)) "Makkiyah" else "Madaniyah"

    val totalAyahs: Int
        get() = ayahs.size
}

data class Ayah(
    @SerializedName("number") val number: Int,
    @SerializedName("arab") val arab: String,
    @SerializedName("id") val id: String
)

object QuranRepository {
    private var cachedSurahs: List<Surah>? = null

    suspend fun getSurahs(context: Context): List<Surah> {
        return withContext(Dispatchers.IO) {
            cachedSurahs?.let { return@withContext it }

            try {
                context.assets.open("quran.json").use { inputStream ->
                    InputStreamReader(inputStream, "UTF-8").use { reader ->
                        val response = Gson().fromJson(reader, QuranResponse::class.java)
                        val cleanedSurahs = response.surahs.map { surah ->
                            surah.copy(
                                ayahs = surah.ayahs.map { ayah ->
                                    ayah.copy(
                                        arab = ayah.arab.replace("\uFEFF", "").trim(),
                                        id = ayah.id.replace("\uFEFF", "").trim()
                                    )
                                }
                            )
                        }
                        cachedSurahs = cleanedSurahs
                        cleanedSurahs
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun getSurahByNumber(context: Context, number: Int): Surah? {
        val all = getSurahs(context)
        return all.find { it.number == number }
    }
}
