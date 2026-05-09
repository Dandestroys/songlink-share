package com.songlink.share.model

import kotlinx.serialization.Serializable

@Serializable
data class HistoryEntry(
    val pageUrl: String,
    val originalUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)
