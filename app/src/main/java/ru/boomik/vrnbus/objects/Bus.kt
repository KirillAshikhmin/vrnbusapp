package ru.boomik.vrnbus.objects

import com.google.android.gms.maps.model.LatLng
import ru.boomik.vrnbus.dal.businessObjects.BusObject
import ru.boomik.vrnbus.utils.toPluralValue
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


/**
 * Created by boomv on 15.03.2018.
 */
enum class BusType {
    Small,
    Medium,
    Big,
    BigLowFloor,
    Trolleybus,
    Unknown
}

class Bus {
    var bus: BusObject = BusObject()
    var timeLeft: Double = Double.MAX_VALUE
    var distance: Double = .0
    var timeDifference: Long = 0
    var localServerTimeDifference: Long = 0

    constructor()

    constructor(route: String) {
        bus.routeName = route
    }

    var timeToArrival: Long = 0
    var arrivalTime: Date? = null


    fun init() {
        timeToArrival = (System.currentTimeMillis() + timeLeft * 60 * 1000).toLong()
        if (timeLeft != Double.MAX_VALUE) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MILLISECOND, (timeLeft * 60 * 1000).toInt())
            arrivalTime = cal.time
        }
    }


    fun getSnippet(): String {

        var bortnumber = ""
        if (!bus.bortName.isNullOrBlank()) bortnumber = "Борт: ${bus.bortName}\n\n"


        var typeString = when (bus.busType) {
            BusObject.BusType.Small -> "Автобус малого класса\n"
            BusObject.BusType.Medium -> "Автобус среднего класса\n"
            BusObject.BusType.Big -> "Автобус большого класса\n"
            BusObject.BusType.Trolleybus -> "Троллейбус\n"
            else -> ""
        }

        if (bus.busType == BusObject.BusType.Big && bus.lowFloor) typeString =
            "Низкопольный автобус большого класса\n"
        if (bus.busType == BusObject.BusType.Medium && bus.lowFloor) typeString =
            "Низкопольный автобус среднего класса\n"
        if (bus.busType == BusObject.BusType.Small && bus.lowFloor) typeString =
            "Низкопольный автобус малого класса\n"

        if (bus.busType == BusObject.BusType.Trolleybus && bus.lowFloor) typeString =
            "Низкопольный троллейбус\n"


        val station = if (bus.nextStationName.isNullOrBlank()) "" else "Следующая остановка:\n\t${bus.nextStationName}\n\n"
        val hasConditioning = if (bus.hasConditioning) "Есть кондиционер\n" else ""
        val speed = "Скорость: ${bus.lastSpeed.toInt()} км/ч"
        var updateTime = ""


        var gosnumber = ""
        if (!bus.licensePlate.isNullOrBlank()) gosnumber = "\nГосномер: ${bus.licensePlate}"

        if (bus.lastTime != null) {

            var difference = timeDifference
            if (bus.lastTime != null) {
                val additionalDifference =
                    (Calendar.getInstance().timeInMillis - bus.lastTime!!.timeInMillis) / 1000 - localServerTimeDifference
                difference = additionalDifference
            }


            val date = bus.lastTime!!.time
            val format1 = SimpleDateFormat("HH:mm:ss", Locale("ru"))
            val timeString = if (abs(difference) >= 60 * 5) {
                toPluralValue((difference / 60).toInt(), "минуту", "минуты", "минут")
            } else toPluralValue(difference, "секунду", "секунды", "секунд")

            updateTime =
                "" + (if (difference < 0 || difference > 0) "\nОбновлено: $timeString назад (${
                    format1.format(date)
                })" else "Обновлено: ${format1.format(date)}")
        }
        return "$bortnumber$station$typeString$hasConditioning$speed$gosnumber$updateTime"
    }

    fun getPosition(): LatLng {
        return LatLng(bus.lastLatitude, bus.lastLongitude)
    }

    fun getAzimuth(): Double {

        //val x = lat - lastLat
        //val y = lon - lastLon
        //return floor(atan2(y, x) * 180 / Math.PI)
        return bus.azimuth.toDouble()
    }

    override fun toString(): String {
        return "${bus.routeName}  $timeLeft"
    }
}
