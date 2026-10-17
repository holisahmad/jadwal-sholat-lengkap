package com.jadwalsholat.lengkap.data

import java.util.Calendar

data class FeaturedVerse(
    val surah: Int,
    val ayah: Int,
    val name: String,
    val arab: String,
    val id: String
)

object FeaturedVersesData {
    val VERSES = listOf(
        FeaturedVerse(
            surah = 2,
            ayah = 286,
            name = "Al-Baqarah",
            arab = "لَا يُكَلِّفُ ٱللَّهُ نَفْسًا إِلَّا وُسْعَهَا ۚ لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا ٱكْتَسَبَتْ ۗ رَبَّنَا لَا تُؤَاخِذْنَآ إِن نَّسِينَآ أَوْ أَخْطَأْنَا",
            id = "Allah tidak membebani seseorang melainkan sesuai dengan kesanggupannya. Ia mendapat pahala (dari kebajikan) yang diusahakannya dan ia mendapat siksa (dari kejahatan) yang dikerjakannya."
        ),
        FeaturedVerse(
            surah = 94,
            ayah = 6,
            name = "Asy-Syarh",
            arab = "إِنَّ مَعَ ٱلْعُسْرِ يُسْرًۭا",
            id = "Sesungguhnya sesudah kesulitan itu ada kemudahan."
        ),
        FeaturedVerse(
            surah = 2,
            ayah = 152,
            name = "Al-Baqarah",
            arab = "فَٱذْكُرُونِىٓ أَذْكُرْكُمْ وَٱشْكُرُوا۟ لِى وَلَا تَكْفُرُونِ",
            id = "Karena itu, ingatlah kamu kepada-Ku niscaya Aku ingat (pula) kepadamu, dan bersyukurlah kepada-Ku, dan janganlah kamu mengingkari (nikmat)-Ku."
        ),
        FeaturedVerse(
            surah = 13,
            ayah = 28,
            name = "Ar-Ra'd",
            arab = "ٱلَّذِينَ ءَامَنُوا۟ وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ ٱللَّهِ ۗ أَلَا بِذِكْرِ ٱللَّهِ تَطْمَئِنُّ ٱلْقُلُوبُ",
            id = "(Yaitu) orang-orang yang beriman dan hati mereka manjadi tenteram dengan mengingat Allah. Ingatlah, hanya dengan mengingati Allah-lah hati menjadi tenteram."
        ),
        FeaturedVerse(
            surah = 2,
            ayah = 255,
            name = "Al-Baqarah",
            arab = "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌۭ وَلَا نَوْمٌۭ",
            id = "Allah, tidak ada Tuhan (yang berhak disembah) melainkan Dia Yang Hidup kekal lagi terus menerus mengurus (makhluk-Nya); tidak mengantuk dan tidak tidur."
        ),
        FeaturedVerse(
            surah = 65,
            ayah = 3,
            name = "At-Talaq",
            arab = "وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ ۚ وَمَن يَتَوَكَّلْ عَلَى ٱللَّهِ فَهُوَ حَسْبُهُۥٓ",
            id = "Dan memberinya rezeki dari arah yang tiada disangka-sangkanya. Dan barangsiapa yang bertawakkal kepada Allah niscaya Allah akan mencukupkan (keperluan)nya."
        )
    )

    fun getDailyVerse(): FeaturedVerse {
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        return VERSES[dayOfYear % VERSES.size]
    }
}
