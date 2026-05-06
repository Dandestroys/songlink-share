package com.songlink.share.api

import com.songlink.share.model.SonglinkResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Songlink / Odesli API.
 *
 * Base URL: https://api.song.link/
 * Endpoint: GET /v1-alpha.1/links
 *
 * Required query parameter:
 *   url — a URL-encoded music platform link (Spotify, Apple Music, YouTube Music, etc.)
 *
 * Optional query parameters (not used here but documented for reference):
 *   userCountry — ISO 3166-1 alpha-2 country code (default: "US")
 *   songIfSingle — if true, treats single-track albums as songs
 *
 * The response contains [SonglinkResponse.pageUrl] which is the canonical
 * song.link smart-link page — the primary value this app extracts and copies.
 */
interface SonglinkApi {

    /**
     * Resolve any supported music platform URL to its Songlink smart-link page.
     *
     * @param url A URL-encoded music track/album URL from any supported platform.
     *            Retrofit will percent-encode this value automatically because
     *            [encoded] is false (the default).
     * @return [SonglinkResponse] containing [SonglinkResponse.pageUrl] and metadata.
     */
    @GET("v1-alpha.1/links")
    suspend fun getLinks(
        @Query("url") url: String
    ): SonglinkResponse
}
