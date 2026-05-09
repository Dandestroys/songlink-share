package com.songlink.share.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SonglinkResponse(
    @SerialName("pageUrl") val pageUrl: String? = null,
    @SerialName("entityUniqueId") val entityUniqueId: String? = null,
    @SerialName("userCountry") val userCountry: String? = null
)

sealed interface SonglinkState {
    data object Idle : SonglinkState
    data object Loading : SonglinkState
    data class Success(val pageUrl: String, val originalUrl: String) : SonglinkState
    data class Error(val message: String) : SonglinkState
}
