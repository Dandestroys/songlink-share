package com.songlink.share.api

import android.util.Log
import com.songlink.share.BuildConfig
import com.songlink.share.model.SonglinkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ApiException(val code: Int) : Exception("HTTP $code")

object SonglinkClient {

    private const val ENDPOINT = "https://api.song.link/v1-alpha.1/links"

    suspend fun getLinks(url: String): SonglinkResponse = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(url, "UTF-8")
        val connection = URL("$ENDPOINT?url=$encoded").openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000

            val code = connection.responseCode
            if (BuildConfig.DEBUG) Log.d("SonglinkClient", "→ $code for $url")

            if (code != HttpURLConnection.HTTP_OK) throw ApiException(code)

            val body = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(body)
            SonglinkResponse(pageUrl = json.optString("pageUrl").ifEmpty { null })
        } finally {
            connection.disconnect()
        }
    }
}
