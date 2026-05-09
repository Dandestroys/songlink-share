package com.songlink.share.model

data class HistoryEntry(
    val pageUrl: String,
    val originalUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)
