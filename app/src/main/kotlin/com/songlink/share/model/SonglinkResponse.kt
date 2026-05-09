package com.songlink.share.model

data class SonglinkResponse(val pageUrl: String? = null)

sealed interface SonglinkState {
    data object Idle : SonglinkState
    data object Loading : SonglinkState
    data class Success(val pageUrl: String, val originalUrl: String) : SonglinkState
    data class Error(val message: String) : SonglinkState
}
