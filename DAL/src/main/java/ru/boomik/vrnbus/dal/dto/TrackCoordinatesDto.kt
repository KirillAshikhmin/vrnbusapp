package ru.boomik.vrnbus.dal.dto

import com.google.gson.annotations.SerializedName

class TrackCoordinatesDto {
    @SerializedName("latitude")
    val latitude: Double = .0

    @SerializedName("longitude")
    val longitude: Double = .0
}