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

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.serialization.Serializable
import java.io.IOException
import java.util.*

/**
 * RouteWithStationsDto
 */
@Serializable
data class RouteWithStationsDto(
        /**
         * Get id
         *
         * @return id
         */
        @SerializedName("id")
        var id: Int,

        /**
         * Get name
         *
         * @return name
         */
        @SerializedName("name")
        var name: String,

        /**
         * Get type
         *
         * @return type
         */
        @SerializedName("type")
        val type: TypeEnum,

        @SerializedName("forwardDirectionStations")
        var forwardDirectionStations: List<Int>,

        @SerializedName("backDirectionStations")
        var backDirectionStations: List<Int>
) {
    fun id(id: Int): RouteWithStationsDto {
        this.id = id
        return this
    }

    fun name(name: String): RouteWithStationsDto {
        this.name = name
        return this
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name, type, forwardDirectionStations, backDirectionStations)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("class RouteWithStationsDto {\n")
        sb.append("    id: ").append(toIndentedString(id)).append("\n")
        sb.append("    name: ").append(toIndentedString(name)).append("\n")
        sb.append("    type: ").append(toIndentedString(type)).append("\n")
        sb.append("    forwardDirectionStations: ").append(toIndentedString(forwardDirectionStations)).append("\n")
        sb.append("    backDirectionStations: ").append(toIndentedString(backDirectionStations)).append("\n")
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

    /**
     * Gets or Sets type
     */
    @JsonAdapter(TypeEnum.Adapter::class)
    enum class TypeEnum(val value: Int) {
        NUMBER_1(1), NUMBER_2(2);

        override fun toString(): String {
            return value.toString()
        }

        class Adapter : TypeAdapter<TypeEnum?>() {
            @Throws(IOException::class)
            override fun write(jsonWriter: JsonWriter, enumeration: TypeEnum?) {
                jsonWriter.value(enumeration!!.value)
            }

            @Throws(IOException::class)
            override fun read(jsonReader: JsonReader): TypeEnum? {
                val value = jsonReader.nextInt()
                return fromValue(value.toString())
            }
        }

        companion object {
            fun fromValue(text: String): TypeEnum? {
                for (b in values()) {
                    if (b.value.toString() == text) {
                        return b
                    }
                }
                return null
            }
        }
    }
}