package ru.boomik.vrnbus

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import ru.boomik.vrnbus.dal.DataServices
import ru.boomik.vrnbus.dal.businessObjects.BusObject
import ru.boomik.vrnbus.dal.remote.RequestStatus
import ru.boomik.vrnbus.dto.ArrivalDto
import ru.boomik.vrnbus.managers.DataManager
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.Route
import ru.boomik.vrnbus.objects.Station
import ru.boomik.vrnbus.objects.StationOnMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object BusService {

    suspend fun loadBusInfoAsync(q: String): List<Bus>? {
        if (q.isBlank()) return null
        val routes = DataServices.CoddPersistentDataService.routes().data ?: return null
        val stations = DataServices.CoddPersistentDataService.stations().data ?: return null
        val query =
                if (q == "*") {
                    routes.map { r -> r.id }.joinToString(",")
                } else {
                    val names = q.split(",").map { it.lowercase() }
                    routes.filter { r -> names.contains(r.name.lowercase()) }.map { r -> r.id }.joinToString(",")
                }

        val buses = DataServices.CoddDataService.getBusesForRoutes(query)

        if (buses.status!=RequestStatus.Ok || buses.data.isNullOrEmpty()) return null

        return try {
            buses.data!!.map { busObject ->
                val route = routes.firstOrNull { it.id == busObject.routeId }
                //TODO: next station id
                val station = stations.firstOrNull { it.id == busObject.id }
                val routename = route?.name ?: ""
                busObject.routeName = routename
                busObject.nextStationName = station?.title ?: ""
                val bus = Bus()
                bus.bus = busObject
                bus.init()
                bus
            }
        } catch (exception: Throwable) {
            Log.e("VrnBus", "Hm..", exception)
            null
        }


    }



    suspend fun loadRouteByNameAsync(routeName: String, forward: Boolean = false): Route? {
        try {
            if (routeName.isBlank()) return null
            var route = if (DataManager.routesCalculated.containsKey(routeName)) DataManager.routesCalculated[routeName] else null
            if (route!=null) return route
            var iteraction = 0
            do {
                delay(500)
                iteraction++
            } while (!DataManager.loaded && iteraction<60)
            val data = DataManager.routes?.firstOrNull { it.name == routeName }
            val tracks = DataManager.tracks
            val stations = DataManager.stations
            if (data == null || tracks.isNullOrEmpty() || stations.isNullOrEmpty()) return null
            val circle = data.type == 2
            var needForward = if (data.type == 1) true else forward
            if (!needForward && data.backward.isEmpty()) needForward = true
            val stationsIds = if (needForward) data.forward else data.backward
            val stationsIdsReverse = if (!needForward) data.forward else data.backward
            if (stationsIds.isEmpty()) return null
            val allStationIds = stations.map { it.id }.toList()

            fun getStations(ids: List<Int>): List<StationOnMap> {
                return ids.filter { s -> allStationIds.contains(s) }.map { id ->
                    val s = stations.first { it.id == id }
                    StationOnMap(s.title, s.id, s.latitude, s.longitude)
                }
            }

            route = Route(data.name, getStations(stationsIds), getStations(stationsIdsReverse))
            fun getRouteStations(stationsList: List<StationOnMap>): List<StationOnMap> {
                val routeStations = mutableListOf<StationOnMap>()
                for ((i, first) in stationsList.withIndex()) {

                    fun addPoints(second : StationOnMap) {
                        val currentEdges = tracks.firstOrNull { it.from == first.id && it.to == second.id }
                        if (currentEdges == null) {
                            routeStations.add(first)
                            return
                        }
                       // routeStations.add(first)
                        val intermediatePoints = currentEdges.coords.map { point -> StationOnMap("", 0, point.lat, point.lon) }
                        routeStations.addAll(intermediatePoints)
                    }

                    if (stationsList.size <= i + 1) {
                        routeStations.add(first)
                        if (circle) addPoints(stationsList[0])
                        break
                    }

                    val second = stationsList[i + 1]
                    addPoints(second)
                }
                return routeStations.toList()
            }

            route.allStations = getRouteStations(route.stations)
            route.allStationsReverse = getRouteStations(route.stationsReverse)
            DataManager.routesCalculated[routeName] = route
            return route

        } catch (exception: Throwable) {
            Log.e("VrnBus", "Hm..", exception)
            return null
        }
    }
}

