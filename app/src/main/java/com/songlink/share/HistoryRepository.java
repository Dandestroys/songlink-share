package com.songlink.share;

import android.content.Context;
import android.content.SharedPreferences;
import com.songlink.share.model.HistoryEntry;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {

    private static final String PREFS = "songlink_history";
    private static final String KEY = "entries";
    private static final int MAX = 20;

    public static List<HistoryEntry> load(Context context) {
        List<HistoryEntry> list = new ArrayList<>();
        String raw = prefs(context).getString(KEY, null);
        if (raw == null) {
            return list;
        }
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(new HistoryEntry(
                    obj.getString("pageUrl"),
                    obj.getString("originalUrl"),
                    obj.getLong("timestamp")
                ));
            }
        } catch (Exception e) {
            // Ignore
        }
        return list;
    }

    public static void add(Context context, HistoryEntry entry) {
        List<HistoryEntry> list = load(context);
        
        // Remove existing items with the same pageUrl
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).getPageUrl().equals(entry.getPageUrl())) {
                list.remove(i);
            }
        }
        
        // Add to front
        list.add(0, entry);
        
        JSONArray arr = new JSONArray();
        int count = Math.min(list.size(), MAX);
        for (int i = 0; i < count; i++) {
            HistoryEntry e = list.get(i);
            try {
                JSONObject obj = new JSONObject();
                obj.put("pageUrl", e.getPageUrl());
                obj.put("originalUrl", e.getOriginalUrl());
                obj.put("timestamp", e.getTimestamp());
                arr.put(obj);
            } catch (Exception ex) {
                // Ignore
            }
        }
        
        prefs(context).edit().putString(KEY, arr.toString()).apply();
    }

    public static void clear(Context context) {
        prefs(context).edit().remove(KEY).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
