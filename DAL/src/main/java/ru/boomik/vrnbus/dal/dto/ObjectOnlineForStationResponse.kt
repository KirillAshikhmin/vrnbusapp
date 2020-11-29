/*
 * Anonymous API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: v1
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
package ru.boomik.vrnbus.dal.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

/**
 * ObjectOnlineForStationResponse
 */
@Serializable
data class ObjectOnlineForStationResponse(
        /**
         * Get serverTime
         *
         * @return serverTime
         */
        @SerializedName("serverTime")
        @Contextual
        val serverTime: String,

        @SerializedName("arrivalBuses") var buses: List<ObjectOnlineDto>
) {

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val objectOnlineForStationResponse = o as ObjectOnlineForStationResponse
        return serverTime == objectOnlineForStationResponse.serverTime &&
                buses == objectOnlineForStationResponse.buses
    }

    override fun hashCode(): Int {
        return Objects.hash(serverTime, buses)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("class ObjectOnlineForStationResponse {\n")
        sb.append("    serverTime: ").append(toIndentedString(serverTime)).append("\n")
        sb.append("    buses: ").append(toIndentedString(buses)).append("\n")
        sb.append("}")
        return sb.toString()
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private fun toIndentedString(o: Any?): String {
        return o?.toString()?.replace("\n", "\n    ") ?: "null"
    }
}