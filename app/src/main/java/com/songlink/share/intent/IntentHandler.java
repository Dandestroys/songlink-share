package com.songlink.share.intent;

import android.content.Intent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntentHandler {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");

    public static String extractUrl(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) {
            return null;
        }

        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText == null) {
            return null;
        }
        sharedText = sharedText.trim();

        if (sharedText.startsWith("http://") || sharedText.startsWith("https://")) {
            return sharedText;
        }

        Matcher matcher = URL_PATTERN.matcher(sharedText);
        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }
}
