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
import java.util.*

/**
 * ObjectOnlineDto
 */
class ObjectOnlineDto {
    @SerializedName("averageSpeed")
    val averageSpeed: Double = .0
    @SerializedName("minutesLeftToBusStop")
    val minutesLeftToBusStop: Double = .0
    @SerializedName("id")
    val id: Int = 0
    @SerializedName("name")
    val name: String? = null
    @SerializedName("objId")
    val objId: Int = 0
    @SerializedName("lastTime")
    val lastTime: String = ""
    @SerializedName("lastLongitude")
    val lastLongitude: Double = .0
    @SerializedName("lastLatitude")
    val lastLatitude: Double = .0
    @SerializedName("lastSpeed")
    val lastSpeed: Double = .0
    @SerializedName("projectId")
    val projectId: Int = 0
    @SerializedName("lastStationId")
    val lastStationId: Int = 0
    @SerializedName("lastStationTime")
    val lastStationTime: String? = null
    @SerializedName("lastRouteId")
    val lastRouteId: Int = 0
    @SerializedName("carTypeId")
    val carTypeId: Int = 0
    @SerializedName("azimuth")
    val azimuth: Int = 0
    @SerializedName("providerId")
    val providerId: Int = 0
    @SerializedName("carBrandId")
    val carBrandId: Int = 0
    @SerializedName("userComment")
    val userComment: String? = null
    @SerializedName("dateInserted")
    val dateInserted: String? = null
    @SerializedName("objectOutput")
    val objectOutput: Boolean? = null
    @SerializedName("objectOutputDate")
    val objectOutputDate: String? = null
    @SerializedName("phone")
    val phone: Long? = null
    @SerializedName("yearRelease")
    val yearRelease: Int = 0
    @SerializedName("dispRouteId")
    val dispRouteId: Int = 0
    @SerializedName("lastAddInfo")
    val lastAddInfo: Int = 0
    @SerializedName("lowfloor")
    val lowfloor: Boolean = false
    @SerializedName("statusName")
    val statusName: String? = null
    @SerializedName("carBrand")
    val carBrand: CarBrandDto? = null
    @SerializedName("provider")
    val provider: ProviderDto? = null
    @SerializedName("route")
    val route: RouteDto? = null
    @SerializedName("project")
    val project: ProjectDto? = null
    @SerializedName("block")
    val block: GranitDto? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val objectOnlineDto = other as ObjectOnlineDto
        return averageSpeed == objectOnlineDto.averageSpeed &&
                minutesLeftToBusStop == objectOnlineDto.minutesLeftToBusStop &&
                id == objectOnlineDto.id &&
                name == objectOnlineDto.name &&
                objId == objectOnlineDto.objId &&
                lastTime == objectOnlineDto.lastTime &&
                lastLongitude == objectOnlineDto.lastLongitude &&
                lastLatitude == objectOnlineDto.lastLatitude &&
                lastSpeed == objectOnlineDto.lastSpeed &&
                projectId == objectOnlineDto.projectId &&
                lastStationId == objectOnlineDto.lastStationId &&
                lastStationTime == objectOnlineDto.lastStationTime &&
                lastRouteId == objectOnlineDto.lastRouteId &&
                carTypeId == objectOnlineDto.carTypeId &&
                azimuth == objectOnlineDto.azimuth &&
                providerId == objectOnlineDto.providerId &&
                carBrandId == objectOnlineDto.carBrandId &&
                userComment == objectOnlineDto.userComment &&
                dateInserted == objectOnlineDto.dateInserted &&
                objectOutput == objectOnlineDto.objectOutput &&
                objectOutputDate == objectOnlineDto.objectOutputDate &&
                phone == objectOnlineDto.phone &&
                yearRelease == objectOnlineDto.yearRelease &&
                dispRouteId == objectOnlineDto.dispRouteId &&
                lastAddInfo == objectOnlineDto.lastAddInfo &&
                lowfloor == objectOnlineDto.lowfloor &&
                statusName == objectOnlineDto.statusName &&
                carBrand == objectOnlineDto.carBrand &&
                provider == objectOnlineDto.provider &&
                route == objectOnlineDto.route &&
                project == objectOnlineDto.project &&
                block == objectOnlineDto.block
    }

    override fun hashCode(): Int {
        return Objects.hash(averageSpeed, minutesLeftToBusStop, id, name, objId, lastTime, lastLongitude, lastLatitude, lastSpeed, projectId, lastStationId, lastStationTime, lastRouteId, carTypeId, azimuth, providerId, carBrandId, userComment, dateInserted, objectOutput, objectOutputDate, phone, yearRelease, dispRouteId, lastAddInfo, lowfloor, statusName, carBrand, provider, route, project, block)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("class ObjectOnlineDto {\n")
        sb.append("    averageSpeed: ").append(toIndentedString(averageSpeed)).append("\n")
        sb.append("    minutesLeftToBusStop: ").append(toIndentedString(minutesLeftToBusStop)).append("\n")
        sb.append("    id: ").append(toIndentedString(id)).append("\n")
        sb.append("    name: ").append(toIndentedString(name)).append("\n")
        sb.append("    objId: ").append(toIndentedString(objId)).append("\n")
        sb.append("    lastTime: ").append(toIndentedString(lastTime)).append("\n")
        sb.append("    lastLongitude: ").append(toIndentedString(lastLongitude)).append("\n")
        sb.append("    lastLatitude: ").append(toIndentedString(lastLatitude)).append("\n")
        sb.append("    lastSpeed: ").append(toIndentedString(lastSpeed)).append("\n")
        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n")
        sb.append("    lastStationId: ").append(toIndentedString(lastStationId)).append("\n")
        sb.append("    lastStationTime: ").append(toIndentedString(lastStationTime)).append("\n")
        sb.append("    lastRouteId: ").append(toIndentedString(lastRouteId)).append("\n")
        sb.append("    carTypeId: ").append(toIndentedString(carTypeId)).append("\n")
        sb.append("    azimuth: ").append(toIndentedString(azimuth)).append("\n")
        sb.append("    providerId: ").append(toIndentedString(providerId)).append("\n")
        sb.append("    carBrandId: ").append(toIndentedString(carBrandId)).append("\n")
        sb.append("    userComment: ").append(toIndentedString(userComment)).append("\n")
        sb.append("    dateInserted: ").append(toIndentedString(dateInserted)).append("\n")
        sb.append("    objectOutput: ").append(toIndentedString(objectOutput)).append("\n")
        sb.append("    objectOutputDate: ").append(toIndentedString(objectOutputDate)).append("\n")
        sb.append("    phone: ").append(toIndentedString(phone)).append("\n")
        sb.append("    yearRelease: ").append(toIndentedString(yearRelease)).append("\n")
        sb.append("    dispRouteId: ").append(toIndentedString(dispRouteId)).append("\n")
        sb.append("    lastAddInfo: ").append(toIndentedString(lastAddInfo)).append("\n")
        sb.append("    lowfloor: ").append(toIndentedString(lowfloor)).append("\n")
        sb.append("    statusName: ").append(toIndentedString(statusName)).append("\n")
        sb.append("    carBrand: ").append(toIndentedString(carBrand)).append("\n")
        sb.append("    provider: ").append(toIndentedString(provider)).append("\n")
        sb.append("    route: ").append(toIndentedString(route)).append("\n")
        sb.append("    project: ").append(toIndentedString(project)).append("\n")
        sb.append("    block: ").append(toIndentedString(block)).append("\n")
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