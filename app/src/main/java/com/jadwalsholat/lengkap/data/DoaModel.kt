package com.jadwalsholat.lengkap.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

data class DoaItem(
    val id: String,
    val title: String,
    val category: String,
    val arab: String,
    val latin: String,
    val arti: String
)

object DoaRepository {
    private var cachedDoas: List<DoaItem>? = null

    suspend fun getDoas(context: Context): List<DoaItem> {
        return withContext(Dispatchers.IO) {
            cachedDoas?.let { return@withContext it }

            try {
                context.assets.open("doa.json").use { inputStream ->
                    InputStreamReader(inputStream, "UTF-8").use { reader ->
                        val itemType = object : TypeToken<List<DoaItem>>() {}.type
                        val list: List<DoaItem> = Gson().fromJson(reader, itemType)
                        cachedDoas = list
                        list
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun getCategories(context: Context): List<String> {
        val doas = getDoas(context)
        val categories = mutableListOf("Semua")
        categories.addAll(doas.map { it.category }.distinct())
        return categories
    }
}
