package com.jadwalsholat.lengkap.data

data class City(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val tz: String
)

object CityData {
    val CITIES = listOf(
        City("jakarta", "Jakarta", -6.2088, 106.8456, "Asia/Jakarta"),
        City("surabaya", "Surabaya", -7.2575, 112.7521, "Asia/Jakarta"),
        City("bandung", "Bandung", -6.9175, 107.6191, "Asia/Jakarta"),
        City("medan", "Medan", 3.5952, 98.6722, "Asia/Jakarta"),
        City("semarang", "Semarang", -6.9667, 110.4167, "Asia/Jakarta"),
        City("makassar", "Makassar", -5.1477, 119.4327, "Asia/Makassar"),
        City("palembang", "Palembang", -2.9761, 104.7754, "Asia/Jakarta"),
        City("depok", "Depok", -6.4025, 106.7942, "Asia/Jakarta"),
        City("tangerang", "Tangerang", -6.1783, 106.6319, "Asia/Jakarta"),
        City("bekasi", "Bekasi", -6.2383, 106.9756, "Asia/Jakarta"),
        City("yogyakarta", "Yogyakarta", -7.7956, 110.3695, "Asia/Jakarta"),
        City("malang", "Malang", -7.9666, 112.6326, "Asia/Jakarta"),
        City("denpasar", "Denpasar", -8.6705, 115.2126, "Asia/Makassar"),
        City("balikpapan", "Balikpapan", -1.2379, 116.8529, "Asia/Makassar"),
        City("padang", "Padang", -0.9471, 100.4172, "Asia/Jakarta"),
        City("pekanbaru", "Pekanbaru", 0.5071, 101.4478, "Asia/Jakarta"),
        City("banjarmasin", "Banjarmasin", -3.3186, 114.5925, "Asia/Makassar"),
        City("pontianak", "Pontianak", -0.0263, 109.3425, "Asia/Jakarta"),
        City("manado", "Manado", 1.4748, 124.8421, "Asia/Makassar"),
        City("banda-aceh", "Banda Aceh", 5.5483, 95.3238, "Asia/Jakarta"),
        City("bandar-lampung", "Bandar Lampung", -5.3971, 105.2668, "Asia/Jakarta"),
        City("bogor", "Bogor", -6.5971, 106.8060, "Asia/Jakarta"),
        City("solo", "Surakarta (Solo)", -7.5755, 110.8243, "Asia/Jakarta"),
        City("jayapura", "Jayapura", -2.5916, 140.6690, "Asia/Jayapura"),
        City("mataram", "Mataram (Lombok)", -8.5833, 116.1167, "Asia/Makassar")
    )

    val DEFAULT_CITY = CITIES[0]

    fun findCity(id: String): City {
        return CITIES.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT_CITY
    }
}
