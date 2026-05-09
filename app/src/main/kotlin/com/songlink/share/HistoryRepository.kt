package com.songlink.share

import android.content.Context
import com.songlink.share.model.HistoryEntry
import org.json.JSONArray
import org.json.JSONObject

internal object HistoryRepository {

    private const val PREFS = "songlink_history"
    private const val KEY = "entries"
    private const val MAX = 20

    fun load(context: Context): List<HistoryEntry> {
        val raw = prefs(context).getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                HistoryEntry(
                    pageUrl = obj.getString("pageUrl"),
                    originalUrl = obj.getString("originalUrl"),
                    timestamp = obj.getLong("timestamp")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    fun add(context: Context, entry: HistoryEntry) {
        val list = load(context).toMutableList()
        list.removeAll { it.pageUrl == entry.pageUrl }
        list.add(0, entry)
        val arr = JSONArray()
        list.take(MAX).forEach { e ->
            arr.put(JSONObject().apply {
                put("pageUrl", e.pageUrl)
                put("originalUrl", e.originalUrl)
                put("timestamp", e.timestamp)
            })
        }
        prefs(context).edit().putString(KEY, arr.toString()).apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
