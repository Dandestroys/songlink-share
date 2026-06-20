package com.songlink.share.model;

public class HistoryEntry {
    private final String pageUrl;
    private final String originalUrl;
    private final long timestamp;

    public HistoryEntry(String pageUrl, String originalUrl) {
        this(pageUrl, originalUrl, System.currentTimeMillis());
    }

    public HistoryEntry(String pageUrl, String originalUrl, long timestamp) {
        this.pageUrl = pageUrl;
        this.originalUrl = originalUrl;
        this.timestamp = timestamp;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
