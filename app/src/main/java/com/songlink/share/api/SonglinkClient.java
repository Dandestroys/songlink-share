package com.songlink.share.api;

import com.songlink.share.BuildConfig;
import com.songlink.share.model.SonglinkResponse;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SonglinkClient {

    public static class ApiException extends Exception {
        private final int code;

        public ApiException(int code) {
            super("HTTP " + code);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private static final String ENDPOINT = "https://api.song.link/v1-alpha.1/links";

    public static SonglinkResponse getLinks(String url) throws Exception {
        String encoded = URLEncoder.encode(url, "UTF-8");
        HttpURLConnection connection = (HttpURLConnection) new URL(ENDPOINT + "?url=" + encoded).openConnection();
        try {
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);

            int code = connection.getResponseCode();
            if (BuildConfig.DEBUG) {
                Log.d("SonglinkClient", "→ " + code + " for " + url);
            }

            if (code != HttpURLConnection.HTTP_OK) {
                throw new ApiException(code);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(sb.toString());
            String pageUrl = json.optString("pageUrl", null);
            if (pageUrl != null && pageUrl.isEmpty()) {
                pageUrl = null;
            }
            return new SonglinkResponse(pageUrl);
        } finally {
            connection.disconnect();
        }
    }
}
