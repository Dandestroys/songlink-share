package com.songlink.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songlink.share.api.ApiException
import com.songlink.share.api.SonglinkClient
import com.songlink.share.intent.IntentHandler
import com.songlink.share.model.HistoryEntry
import com.songlink.share.model.SonglinkState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShareViewModel : ViewModel() {

    private val _state = MutableStateFlow<SonglinkState>(SonglinkState.Idle)
    val state: StateFlow<SonglinkState> = _state.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    fun handleIntent(intent: Intent?, context: Context) {
        _history.value = HistoryRepository.load(context)

        val url = IntentHandler.extractUrl(intent)
            ?: run {
                if (intent?.action == Intent.ACTION_SEND) {
                    _state.value = SonglinkState.Error(
                        "No URL found in the shared text. Please share a link from a music app."
                    )
                }
                return
            }

        fetchSonglink(url, context)
    }

    fun fetchSonglink(url: String, context: Context) {
        _state.value = SonglinkState.Loading

        viewModelScope.launch {
            try {
                val response = SonglinkClient.getLinks(url)

                val pageUrl = response.pageUrl?.takeIf { it.isNotBlank() }
                    ?: run {
                        _state.value = SonglinkState.Error(
                            "The Songlink API returned a response without a pageUrl. " +
                            "The track may not be available in your region."
                        )
                        return@launch
                    }

                copyToClipboard(context, pageUrl)

                HistoryRepository.add(context, HistoryEntry(pageUrl = pageUrl, originalUrl = url))
                _history.value = HistoryRepository.load(context)

                _state.value = SonglinkState.Success(pageUrl = pageUrl, originalUrl = url)

            } catch (e: ApiException) {
                _state.value = SonglinkState.Error(
                    when (e.code) {
                        400 -> "The URL wasn't recognised by Songlink (HTTP 400). Make sure you're sharing a supported music link."
                        404 -> "Songlink couldn't find this track (HTTP 404)."
                        429 -> "Too many requests — please wait a moment and try again."
                        in 500..599 -> "Songlink's servers are having trouble (HTTP ${e.code}). Try again later."
                        else -> "API error (HTTP ${e.code})"
                    }
                )
            } catch (e: java.net.UnknownHostException) {
                _state.value = SonglinkState.Error("No internet connection. Please check your network and try again.")
            } catch (e: java.net.SocketTimeoutException) {
                _state.value = SonglinkState.Error("The request timed out. Please try again.")
            } catch (e: Exception) {
                _state.value = SonglinkState.Error("Unexpected error: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Songlink URL", text))
    }

    fun clearHistory(context: Context) {
        HistoryRepository.clear(context)
        _history.value = emptyList()
    }

    fun reset() {
        _state.value = SonglinkState.Idle
    }
}
