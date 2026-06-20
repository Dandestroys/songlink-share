package com.songlink.share.model;

public class SonglinkResponse {
    private final String pageUrl;

    public SonglinkResponse(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getPageUrl() {
        return pageUrl;
    }
}
