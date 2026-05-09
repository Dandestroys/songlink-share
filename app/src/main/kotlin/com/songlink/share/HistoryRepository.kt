package com.songlink.share

import android.content.Context
import com.songlink.share.model.HistoryEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object HistoryRepository {

    private const val PREFS = "songlink_history"
    private const val KEY = "entries"
    private const val MAX = 20
    private val json = Json { ignoreUnknownKeys = true }

    fun load(context: Context): List<HistoryEntry> {
        val raw = prefs(context).getString(KEY, null) ?: return emptyList()
        return try { json.decodeFromString(raw) } catch (e: Exception) { emptyList() }
    }

    fun add(context: Context, entry: HistoryEntry) {
        val list = load(context).toMutableList()
        list.removeAll { it.pageUrl == entry.pageUrl }
        list.add(0, entry)
        prefs(context).edit().putString(KEY, json.encodeToString(list.take(MAX))).apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
