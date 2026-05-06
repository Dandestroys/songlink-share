package com.songlink.share.intent

import android.content.Intent

/**
 * Utility object responsible for extracting a music platform URL from an
 * Android share [Intent].
 *
 * Music apps (Spotify, YouTube Music, Apple Music, Tidal, Deezer, SoundCloud,
 * etc.) share track links via [Intent.ACTION_SEND] with MIME type "text/plain".
 * The shared text is stored in [Intent.EXTRA_TEXT] and may contain surrounding
 * copy such as:
 *
 *   "Check out 'Bohemian Rhapsody' on Spotify: https://open.spotify.com/track/…"
 *
 * This object handles that case by scanning the text for the first valid URL.
 */
object IntentHandler {

    /**
     * Regex that matches a URL starting with http:// or https://.
     *
     * Matches greedily up to the first whitespace or end of string, which is
     * sufficient for URLs embedded in plain-text share payloads from music apps.
     */
    private val URL_REGEX = Regex("""https?://\S+""")

    /**
     * Extracts the first valid URL from an [Intent.ACTION_SEND] intent.
     *
     * Steps:
     * 1. Verify the action is [Intent.ACTION_SEND].
     * 2. Read [Intent.EXTRA_TEXT].
     * 3. If the entire string is a URL, return it directly.
     * 4. Otherwise, scan for the first embedded URL with [URL_REGEX].
     * 5. Return null if no URL is found, which the ViewModel converts to an error.
     *
     * @param intent The intent received by [com.songlink.share.MainActivity].
     * @return The first URL found in the shared text, or null.
     */
    fun extractUrl(intent: Intent?): String? {
        // Only handle ACTION_SEND intents; ignore launcher starts and others
        if (intent?.action != Intent.ACTION_SEND) return null

        // EXTRA_TEXT holds the raw shared string from the music app
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            ?.trim()
            ?: return null

        // Fast path: the entire payload is already a bare URL
        if (sharedText.startsWith("http://") || sharedText.startsWith("https://")) {
            return sharedText
        }

        // Slow path: extract the first URL embedded in a longer sentence
        // e.g. "Listen to X on Spotify https://open.spotify.com/track/…"
        return URL_REGEX.find(sharedText)?.value
    }
}
