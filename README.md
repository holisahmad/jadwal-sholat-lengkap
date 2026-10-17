# Jadwal Sholat Lengkap, Kiblat, Al-Qur'an, Doa & Zakat

Aplikasi native Android berkinerja tinggi yang dibangun dengan **Jetpack Compose + Kotlin**, menargetkan **Android 16 (API 36)**. Aplikasi ini dirancang khusus untuk memenuhi kebutuhan ibadah harian umat Muslim di Indonesia dengan kalkulasi astronomis akurat, navigasi intuitif, desain modern bernuansa islami, serta kepatuhan penuh terhadap kebijakan Google AdMob dan Google Play Store.

---

## Fitur Utama

1. **Jadwal Sholat 5 Waktu & Hitung Mundur Real-Time:**
   - Waktu Imsak, Subuh, Terbit, Dzuhur, Ashar, Maghrib, dan Isya.
   - Hitung mundur (*countdown*) detik demi detik menuju waktu sholat berikutnya.
   - Pilihan 25+ kota besar di seluruh Indonesia (WIB, WITA, WIT) atau deteksi GPS otomatis.
   - Pelacak streak ibadah & checklist centang sholat wajib harian untuk menjaga konsistensi.
2. **Jadwal Sholat Bulanan:**
   - Kalender dan tabel jadwal waktu sholat lengkap satu bulan penuh.
3. **Arah Kiblat (Qibla Compass):**
   - Kompas visual real-time dengan sensor internal perangkat yang mendeteksi arah Ka'bah dari kota pengguna.
4. **Al-Qur'an 30 Juz & 114 Surah (100% Bebas Iklan):**
   - Teks Arab khat Utsmani yang jernih, transliterasi Latin, dan terjemahan resmi Bahasa Indonesia per ayat.
   - Fitur bookmark surah dan penanda terakhir dibaca (*last read*).
   - Fitur pencarian cepat nama surah.
   - **Bebas Iklan Sepenuhnya** pada halaman baca Al-Qur'an untuk menjaga adab dan kekhusyukan ibadah.
5. **Kumpulan Doa Harian & Dzikir:**
   - Doa Harian, Doa Sholat, Doa Makan, Doa Perjalanan, dan Doa Perlindungan.
   - Teks Arab, bacaan latin, dan arti bahasa Indonesia, dilengkapi tombol salin teks dan bagikan (*share*).
6. **Kalkulator Zakat Mal:**
   - Hisab otomatis zakat tabungan, simpanan emas, perak, dan harta perniagaan.
   - Acuan nisab setara 85 gram emas dengan harga emas per gram yang dapat disesuaikan.
7. **Kalender Hijriah & Masehi Terintegrasi:**
   - Penyesuaian tanggal Hijriah lokal otomatis.

---

## Checklist Kepatuhan AdMob & Google Play (Sesuai Aturan A.4.6)

| Poin Kepatuhan | Status | Catatan |
| :--- | :---: | :--- |
| **Ad Unit ID Resmi Google (Test Mode)** | **PASS** | Menggunakan test unit ID resmi selama fase development. Wajib diganti ke ID produksi saat rilis. |
| **Placeholder AdMob App ID di AndroidManifest.xml** | **PASS** | Telah diberi penanda jelas `<!-- GANTI DENGAN AD APP ID ASLI -->`. |
| **Tidak ada iklan menutupi tombol / navigasi** | **PASS** | Banner adaptive ditempatkan di bagian bawah dengan padding aman tanpa menghalangi tombol interaktif. |
| **Interstitial tidak muncul saat Splash / Launch** | **PASS** | Tidak ada interstitial pada saat aplikasi pertama kali dibuka. |
| **Tombol Close yang Jelas pada Interstitial** | **PASS** | Menggunakan Google AdMob Fullscreen Callback standar resmi. |
| **Frequency Capping Interstitial (60 Detik + Event)** | **PASS** | Diterapkan di `AdManager` dengan batasan jeda minimal 60 detik dan akumulasi 3 event transisi. |
| **Halaman Ibadah Khusus Bebas Iklan** | **PASS** | Halaman baca ayat Al-Qur'an 100% bebas dari segala jenis iklan. |
| **Ad Content Rating G (General)** | **PASS** | Dikonfigurasi `MAX_AD_CONTENT_RATING_G` untuk mencegah iklan sensitif. |
| **UMP Consent Management SDK** | **PASS** | Terintegrasi untuk kepatuhan regulasi privasi global (GDPR). |
| **Privacy Policy Siap Pakai** | **PASS** | Disediakan dalam format Markdown (`privacy_policy.md`) dan HTML siap hosting (`privacy_policy.html`). |
| **Target SDK Terbaru** | **PASS** | Target SDK 36 (Android 16), Compile SDK 36, Min SDK 26 (Android 8.0+). |

---

## Cara Build & Menjalankan Project

### 1. Prasyarat
- JDK 17
- Android SDK Platform 36 (Android 16) & Build-Tools terbaru
- Android Studio Iguana / Ladybug atau yang lebih baru

### 2. Compile & Build APK
Jalankan perintah berikut di terminal:
```bash
./gradlew assembleDebug
```
File APK debug akan dihasilkan di:
`app/build/outputs/apk/debug/app-debug.apk`

### 3. Mengganti Test Ad Unit ID ke ID Produksi Sebelum Rilis
1. Buka `app/src/main/AndroidManifest.xml`, ganti nilai `com.google.android.gms.ads.APPLICATION_ID` dengan App ID AdMob asli Anda.
2. Buka `app/src/main/java/com/jadwalsholat/lengkap/ads/AdManager.kt`, ganti konstanta `BANNER_TEST_ID` dan `INTERSTITIAL_TEST_ID` dengan ID unit iklan produksi dari dashboard Google AdMob Anda.
