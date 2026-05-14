package com.example.p71.data.remote

import com.google.gson.annotations.SerializedName

data class NominatimSearchResult(
    @SerializedName("place_id") val placeId: Long,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String
) {
    val latitude: Double get() = lat.toDouble()
    val longitude: Double get() = lon.toDouble()
}

data class NominatimReverseResult(
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("lat") val lat: String?,
    @SerializedName("lon") val lon: String?
)