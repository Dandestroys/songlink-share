package com.songlink.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songlink.share.api.RetrofitClient
import com.songlink.share.intent.IntentHandler
import com.songlink.share.model.SonglinkState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for [MainActivity].
 *
 * Owns the full lifecycle of a Songlink lookup:
 *   Idle → Loading → Success | Error
 *
 * The ViewModel survives configuration changes (screen rotation) so the user
 * never loses a successfully fetched Songlink URL when they rotate their phone.
 */
class ShareViewModel : ViewModel() {

    private val _state = MutableStateFlow<SonglinkState>(SonglinkState.Idle)

    /** Observe this from Compose with [collectAsStateWithLifecycle]. */
    val state: StateFlow<SonglinkState> = _state.asStateFlow()

    /**
     * Entry point called from [MainActivity.onNewIntent] and [MainActivity.onCreate].
     *
     * Extracts the first URL from [intent] via [IntentHandler], then launches a
     * coroutine to call the Songlink API. After a successful response:
     *  - Verifies [pageUrl] is present (errors out if missing).
     *  - Copies [pageUrl] to the system clipboard automatically.
     *  - Transitions state to [SonglinkState.Success].
     *
     * @param intent The [Intent.ACTION_SEND] intent received by the activity.
     * @param context Application context used to access [ClipboardManager].
     */
    fun handleIntent(intent: Intent?, context: Context) {
        // Extract URL from the share intent; bail out if none found
        val url = IntentHandler.extractUrl(intent)
            ?: run {
                if (intent?.action == Intent.ACTION_SEND) {
                    // Intent was a share but had no recognisable URL
                    _state.value = SonglinkState.Error(
                        "No URL found in the shared text. " +
                        "Please share a link from a music app."
                    )
                }
                // If opened via the launcher (no ACTION_SEND), stay Idle
                return
            }

        fetchSonglink(url, context)
    }

    /**
     * Calls the Songlink API for [url] and updates [state].
     *
     * Separated from [handleIntent] so the UI can expose a "Retry" button that
     * re-triggers the same network call without re-parsing the intent.
     *
     * @param url A music platform URL to resolve (not yet encoded — Retrofit handles that).
     * @param context Used to access [ClipboardManager] on success.
     */
    fun fetchSonglink(url: String, context: Context) {
        _state.value = SonglinkState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getLinks(url)

                // pageUrl is the only field we care about. Per the app spec, we
                // treat a missing or blank pageUrl as an explicit error rather
                // than falling back to any platform-specific URL.
                val pageUrl = response.pageUrl
                    ?.takeIf { it.isNotBlank() }
                    ?: run {
                        _state.value = SonglinkState.Error(
                            "The Songlink API returned a response without a " +
                            "pageUrl field. The track may not be available in " +
                            "your region, or the link format isn't supported."
                        )
                        return@launch
                    }

                // Automatically copy the smart-link to clipboard on success
                copyToClipboard(context, pageUrl)

                _state.value = SonglinkState.Success(
                    pageUrl = pageUrl,
                    originalUrl = url
                )

            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                _state.value = SonglinkState.Error(
                    when (code) {
                        400 -> "The URL wasn't recognised by Songlink (HTTP 400). " +
                               "Make sure you're sharing a supported music link."
                        404 -> "Songlink couldn't find this track (HTTP 404)."
                        429 -> "Too many requests — please wait a moment and try again."
                        in 500..599 -> "Songlink's servers are having trouble (HTTP $code). Try again later."
                        else -> "API error (HTTP $code): ${e.message()}"
                    }
                )
            } catch (e: java.net.UnknownHostException) {
                _state.value = SonglinkState.Error(
                    "No internet connection. Please check your network and try again."
                )
            } catch (e: java.net.SocketTimeoutException) {
                _state.value = SonglinkState.Error(
                    "The request timed out. The Songlink API may be slow — please try again."
                )
            } catch (e: Exception) {
                _state.value = SonglinkState.Error(
                    "Unexpected error: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    /**
     * Copies [text] to the Android system clipboard using a [ClipboardManager].
     *
     * The label "Songlink URL" appears in clipboard history UI on supported
     * Android versions (Android 13+).
     */
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Songlink URL", text)
        clipboard.setPrimaryClip(clip)
    }

    /** Resets state back to [SonglinkState.Idle] (e.g., when user presses back). */
    fun reset() {
        _state.value = SonglinkState.Idle
    }
}
