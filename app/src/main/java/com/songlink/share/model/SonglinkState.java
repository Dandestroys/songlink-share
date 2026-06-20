package com.songlink.share.model;

public class SonglinkState {
    public enum Type { IDLE, LOADING, SUCCESS, ERROR }
    
    private final Type type;
    private final String pageUrl;
    private final String originalUrl;
    private final String errorMessage;

    private SonglinkState(Type type, String pageUrl, String originalUrl, String errorMessage) {
        this.type = type;
        this.pageUrl = pageUrl;
        this.originalUrl = originalUrl;
        this.errorMessage = errorMessage;
    }

    public static SonglinkState idle() {
        return new SonglinkState(Type.IDLE, null, null, null);
    }

    public static SonglinkState loading() {
        return new SonglinkState(Type.LOADING, null, null, null);
    }

    public static SonglinkState success(String pageUrl, String originalUrl) {
        return new SonglinkState(Type.SUCCESS, pageUrl, originalUrl, null);
    }

    public static SonglinkState error(String message) {
        return new SonglinkState(Type.ERROR, null, null, message);
    }

    public Type getType() {
        return type;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
