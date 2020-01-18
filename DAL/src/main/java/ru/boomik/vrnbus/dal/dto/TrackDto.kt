package ru.boomik.vrnbus.dal.dto

import com.google.gson.annotations.SerializedName

class TrackDto {
    @SerializedName("startBusStationId")
    val startBusStationId: Int = 0

    @SerializedName("endBusStationId")
    val endBusStationId: Int = 0

    @SerializedName("trackCoordinates")
    val trackCoordinates: List<TrackCoordinatesDto>? = null
}