package com.songlink.share.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level response from the Songlink API.
 *
 * Full API docs: https://www.notion.so/API-d0ebe08a5e304a55928405eb682f6741
 *
 * Example response (abbreviated):
 * {
 *   "entityUniqueId": "SPOTIFY_SONG::6rqhFgbbKwnb9MLmUQDhG6",
 *   "userCountry": "US",
 *   "pageUrl": "https://song.link/s/6rqhFgbbKwnb9MLmUQDhG6",   <-- this is what we want
 *   "entitiesByUniqueId": { ... },
 *   "linksByPlatform": {
 *     "spotify": { "url": "...", "nativeAppUriMobile": "...", ... },
 *     "youtube": { "url": "...", ... },
 *     ...
 *   }
 * }
 *
 * We only map the fields we actually need. Unknown fields are silently ignored
 * because [kotlinx.serialization.json.Json] is configured with ignoreUnknownKeys = true
 * in [com.songlink.share.api.RetrofitClient].
 */
@Serializable
data class SonglinkResponse(

    /**
     * The canonical Songlink smart-link page for this track.
     * This is the primary output of the app — the URL that gets copied to clipboard.
     *
     * Format: "https://song.link/s/<id>" or "https://album.link/s/<id>"
     *
     * We treat a null or blank [pageUrl] as an error rather than falling back to
     * any platform-specific URL, per the app's requirements.
     */
    @SerialName("pageUrl")
    val pageUrl: String? = null,

    /**
     * The unique ID of the detected entity (song/album/podcast).
     * Included for debugging convenience.
     */
    @SerialName("entityUniqueId")
    val entityUniqueId: String? = null,

    /**
     * The country used to resolve platform links.
     */
    @SerialName("userCountry")
    val userCountry: String? = null
)

/**
 * Represents the possible states while fetching a Songlink URL.
 */
sealed interface SonglinkState {
    /** No share intent received yet; app opened standalone. */
    data object Idle : SonglinkState

    /** Actively calling the Songlink API. */
    data object Loading : SonglinkState

    /**
     * API call succeeded and [pageUrl] was present in the response.
     * @param pageUrl The canonical song.link URL ready to share/copy.
     * @param originalUrl The URL originally extracted from the share intent.
     */
    data class Success(
        val pageUrl: String,
        val originalUrl: String
    ) : SonglinkState

    /**
     * Something went wrong. A human-readable [message] is provided.
     * @param message Reason for failure shown to the user.
     */
    data class Error(val message: String) : SonglinkState
}
