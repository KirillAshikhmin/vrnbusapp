package ru.boomik.vrnbus.managers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.Log
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.dal.businessObjects.BusObject
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.Route
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.CustomUrlTileProvider
import ru.boomik.vrnbus.utils.createImageRoundedBitmap
import java.util.*
import kotlin.random.Random


/**
 * Created by boomv on 18.03.2018.
 */
class MapManager(activity: Activity, mapFragment: SupportMapFragment) : OnMapReadyCallback {
    private var mLastZoom: Float = -1f
    private var mActivity: Activity = activity
    private var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mMap: GoogleMap
    private lateinit var mReadyCallback: () -> Unit

    private var mBusesMarkers: List<Marker>? = null
    private var mRouteOnMap: String? = null
    private var mRoutesOnMap: List<Polyline>? = null
    private var mTraffic: Boolean = false

    private var small: Drawable?
    private var medium: Drawable?
    private var big: Drawable?
    private var bigFloor: Drawable?
    private var middleFloor: Drawable?
    private var smallFloor: Drawable?
    private var trolleybus: Drawable?

    init {
        MapsInitializer.initialize(activity.applicationContext)

        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity)

        DataBus.subscribe<Boolean>(DataBus.Traffic) {
            setTrafficJam(it.data)
        }
        DataBus.subscribe<Boolean>(Consts.SETTINGS_ROTATE) {
            loadRotate()
        }
        DataBus.subscribe<List<Bus>>(DataBus.BusToMap) {
            showBusesOnMap(it.data)
        }


        small = ContextCompat.getDrawable(mActivity, R.drawable.ic_bus_small)
        medium = ContextCompat.getDrawable(mActivity, R.drawable.ic_bus_middle)
        big = ContextCompat.getDrawable(mActivity, R.drawable.ic_bus_large)
        bigFloor = ContextCompat.getDrawable(mActivity, R.drawable.ic_bus_large_low_floor)
        middleFloor = ContextCompat.getDrawable(mActivity, R.drawable.ic_bus_middle_low_floor)
        smallFloor = ContextCompat.getDrawable(mActivity, R.drawable.ic_bus_small_low_floor)
        trolleybus = ContextCompat.getDrawable(mActivity, R.drawable.ic_trolleybus)
    }


    fun subscribeReady(callback: () -> Unit) {
        mReadyCallback = callback
    }

    private var stationVisibleSmall: Boolean = true
    private val stationVisibleZoomSmall = 14
    private var initPosition: LatLng? = null
    private var initZoom: Float = 12F
    private var mBusesMarkerType = -1
    private val markerZoom = 14
    private val markerSmallZoom = 12

    private var mStationMarkersSmall: List<Marker> = listOf()
    private var mInFavoriteStationSmallMarkers: MutableList<Marker> = mutableListOf()

    private var mFavoriteStationMarkers: MutableList<Marker> = mutableListOf()

    private lateinit var mAllStations: List<StationOnMap>
    private var favoriteStations: List<Int>? = null
    var padding: Rect = Rect()


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setPadding(padding.left, (padding.top + 64 * mActivity.resources.displayMetrics.density).toInt(), padding.right, padding.bottom)
        val currentNightMode = mActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        val night = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        try {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mActivity, if (night) R.raw.map_style_json_dark else R.raw.map_style_json))
        } catch (e: Resources.NotFoundException) {
            Log.e("Can't find style. Error: ", e)
        }

        val showOsm = SettingsManager.getBool(Consts.SETTINGS_OSM)

        if (showOsm) {
            val r = Random(System.currentTimeMillis()).nextInt(1, 3)
            val url = if (!night) when (r) {
                1 -> Consts.TILES_URL_A
                2 -> Consts.TILES_URL_B
                3 -> Consts.TILES_URL_C
                else -> Consts.TILES_URL_A
            } else when (r) {
                1 -> Consts.TILES_URL_DARK_A
                2 -> Consts.TILES_URL_DARK_B
                3 -> Consts.TILES_URL_DARK_C
                else -> Consts.TILES_URL_DARK_A
            }
            val mTileProvider = CustomUrlTileProvider(256, 256, url)
            mMap.addTileOverlay(TileOverlayOptions().tileProvider(mTileProvider).zIndex(0f))
            mMap.mapType = GoogleMap.MAP_TYPE_NONE
        }

        mMap.setMaxZoomPreference(19f)

        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isIndoorLevelPickerEnabled = false
        mMap.isTrafficEnabled = mTraffic
        loadRotate()


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Consts.INITIAL_POSITION, Consts.INITIAL_ZOOM))

        mMap.setOnInfoWindowClickListener {
            DataBus.sendEvent(DataBus.BusClick, it.tag as Bus)
        }
        mMap.setOnMarkerClickListener {
            if (it.tag is StationOnMap) {
                DataBus.sendEvent(DataBus.StationClick, it.tag as StationOnMap)
                return@setOnMarkerClickListener true
            }
            false
        }
        mMap.setOnCameraMoveListener {
            checkZoom()
        }
        mMap.setOnCameraIdleListener {
            checkZoom()
        }
        mReadyCallback()

        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {

                val info = LinearLayout(mActivity)
                val d = (mActivity.resources.displayMetrics.density * 8).toInt()
                info.setPadding(d, d, d, d)
                info.orientation = LinearLayout.VERTICAL

                val title = TextView(mActivity)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)

                title.text = "${marker.title}\n"

                val snippet = TextView(mActivity)
                snippet.setTextColor(Color.GRAY)
                if (marker.tag != null) {
                    val bus = marker.tag as Bus
                    snippet.text = bus.getSnippet()
                } else
                    snippet.text = marker.snippet

                info.addView(title)
                info.addView(snippet)

                return info
            }
        })


        val initPos = initPosition
        if (initPos != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initPos, initZoom))
            checkZoom()
        } else if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.continueWith {
                val location = it.result
                if (location != null && it.isSuccessful) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val distance = distanceBetween(latLng, LatLng(51.673909, 39.207646))
                    if (distance < 20000)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                }
            }
        }
        try {
            mActivity.reportFullyDrawn()
        } catch (e: Throwable) {
        }
        initPosition = null
    }

    private fun distanceBetween(latLng1: LatLng, latLng2: LatLng): Float {

        val loc1 = Location(LocationManager.GPS_PROVIDER)
        loc1.latitude = latLng1.latitude
        loc1.longitude = latLng1.longitude

        val loc2 = Location(LocationManager.GPS_PROVIDER)
        loc2.latitude = latLng2.latitude
        loc2.longitude = latLng2.longitude
        return loc1.distanceTo(loc2)
    }

    private var mShowSmall: Boolean = false

    private fun checkZoom() {
        if (!::mMap.isInitialized) return
        val zoom = mMap.cameraPosition.zoom
        if (mLastZoom == zoom) return
        mLastZoom = mMap.cameraPosition.zoom


        var showSmall = false

        if (mMap.cameraPosition.zoom >= stationVisibleZoomSmall) showSmall = true
        mShowSmall = showSmall

        showBusesOnMap(mBuses, false, false)

        if (stationVisibleSmall == showSmall) return
        setVisibleStationSmall(showSmall)
    }

    private fun setVisibleStationSmall(visible: Boolean) {
        mStationMarkersSmall.filter { !mInFavoriteStationSmallMarkers.contains(it) }.forEach { it.isVisible = visible }
        stationVisibleSmall = visible
    }

    fun clearBusesOnMap() {
        mBusesMarkers?.forEach { it.remove() }
        mBusesMarkers = null
        mBuses = null
        mBusesMarkerType = -1
    }

    private var mBuses: List<Bus>? = null

    fun showBusesOnMap(buses: List<Bus>?, ignoreType: Boolean = true, clearRoute: Boolean = true) {

        if (clearRoute) clearRoutes()

        if (buses == null || !buses.any()) {
            clearBusesOnMap()
            return
        }
        if (!::mMap.isInitialized)
            return

        var neededType = 0
        if (mMap.cameraPosition.zoom >= markerZoom) {
            neededType = 2
        } else if (mMap.cameraPosition.zoom >= markerSmallZoom) {
            neededType = 1
        }

        if (!ignoreType && mBusesMarkerType == neededType) return
        clearBusesOnMap()
        mBuses = buses
        mBusesMarkerType = neededType

        val d = mActivity.resources.displayMetrics.density
        var size = (36 * d).toInt()
        if (neededType == 0) size /= 2


        val newBusesMarkers = buses.mapNotNull {
            var typeIcon = when (it.bus.busType) {
                BusObject.BusType.Small -> small
                BusObject.BusType.Medium -> medium
                BusObject.BusType.Big -> big
                BusObject.BusType.Trolleybus -> trolleybus
                else -> big
            }
            if (it.bus.busType == BusObject.BusType.Big && it.bus.lowFloor) typeIcon = bigFloor
            if (it.bus.busType == BusObject.BusType.Medium && it.bus.lowFloor) typeIcon = middleFloor
            if (it.bus.busType == BusObject.BusType.Small && it.bus.lowFloor) typeIcon = smallFloor

            val color = when (it.bus.busType) {
                BusObject.BusType.Small -> Consts.COLOR_BUS_SMALL
                BusObject.BusType.Medium -> Consts.COLOR_BUS_MEDIUM
                BusObject.BusType.Big -> Consts.COLOR_BUS
                BusObject.BusType.Trolleybus -> Consts.COLOR_BUS_TROLLEYBUS
                else -> Consts.COLOR_BUS
            }

                val route = DataManager.routes?.firstOrNull { r -> r.id == it.bus.routeId }
                if (route != null) {
                    it.bus.routeName = route.name
                    if (route.name.startsWith("Т")) it.bus.busType = BusObject.BusType.Trolleybus

                    if (it.bus.nextStationName.isNullOrEmpty()) {
                        var forward = true
                        var index = route.forward.indexOfFirst { r -> r == it.bus.lastStationId }
                        if (index < 0) {
                            index = route.backward.indexOfFirst { r -> r == it.bus.lastStationId }
                            forward = false
                        }
                        if (index >= 0) {
                            val nextIndex = ++index
                            val next = if (forward && route.forward.size > nextIndex) route.forward[nextIndex]
                            else if (!forward && route.backward.size > nextIndex) route.backward[nextIndex]
                            else 0

                            val station = DataManager.stations?.firstOrNull { r -> r.id == next }
                            if (station != null) it.bus.nextStationName = station.title
                        }
                    }
                }



            val options = MarkerOptions().position(it.getPosition()).title(it.bus.routeName).zIndex(1.0f).flat(true)
            options.snippet(it.getSnippet())
            try {
                options.icon(BitmapDescriptorFactory.fromBitmap(createImageRoundedBitmap(neededType, typeIcon, size, it.bus.routeName, it.getAzimuth(), color)))
            } catch (e: Throwable) {
                try {
                    options.icon(BitmapDescriptorFactory.fromBitmap(createImageRoundedBitmap(if (neededType >= 2) 1 else neededType, typeIcon, size, it.bus.routeName, it.getAzimuth(), color)))
                } catch (e: Throwable) {

                }
            }
            if (neededType == 2) {
                options.anchor(1 / 6f, .5f)
                options.infoWindowAnchor(1 / 6f, .2f)
            } else {
                options.anchor(.5f, .5f)
                options.infoWindowAnchor(.5f, .2f)
            }
            var difference = it.timeDifference
            if (it.bus.lastTime != null) {
                val additionalDifference = (Calendar.getInstance().timeInMillis - it.bus.lastTime!!.timeInMillis) / 1000 - it.localServerTimeDifference
                difference = additionalDifference
            }

            when {
                difference > 180L -> options.alpha(0.5f)
                difference > 60L -> options.alpha(0.8f)
            }

            val marker = mMap.addMarker(options)
            marker?.tag = it
            marker
        }

        mBusesMarkers = newBusesMarkers
    }

    fun showStations(stationsOnMap: List<StationOnMap>?) {
        if (stationsOnMap == null) return
        if (::mAllStations.isInitialized && mAllStations.any()) return

        mAllStations = stationsOnMap

        if (!::mMap.isInitialized)
            return

        var showSmall = false

        if (mMap.cameraPosition.zoom >= stationVisibleZoomSmall) showSmall = true

        val iconSmall: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station_small)


        stationVisibleSmall = showSmall


        val newStationSmallMarkers = stationsOnMap.mapNotNull {
            val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.getTitle()).icon(iconSmall).zIndex(0.8f).anchor(.5f, .5f).visible(showSmall))
            marker?.tag = it
            marker
        }

        mStationMarkersSmall = newStationSmallMarkers

        loadPreferences()

        checkZoom()
    }

    private fun loadPreferences() {
        mInFavoriteStationSmallMarkers = mutableListOf()
        loadFavoriteStations()
        DataBus.subscribe<Pair<Int, Boolean>>(DataBus.FavoriteStation) {
            loadFavoriteStations()
        }
    }

    private fun loadFavoriteStations() {
        favoriteStations = SettingsManager.getIntArray(Consts.SETTINGS_FAVORITE_STATIONS)
        checkFavoritesStations()
    }

    private fun loadRotate() {
        if (!::mMap.isInitialized) return
        val rotate = SettingsManager.getBool(Consts.SETTINGS_ROTATE)

        if (rotate) {
            mMap.uiSettings.isCompassEnabled = true
            mMap.uiSettings.isRotateGesturesEnabled = true
            mMap.uiSettings.isTiltGesturesEnabled = true
        } else {
            mMap.uiSettings.isCompassEnabled = false
            mMap.uiSettings.isRotateGesturesEnabled = false
            mMap.uiSettings.isTiltGesturesEnabled = false

            val newCamPos = CameraPosition(mMap.cameraPosition.target, mMap.cameraPosition.zoom, 0f, 0f)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos))
        }
    }

    private fun checkFavoritesStations() {

        if (favoriteStations != null) {

            mInFavoriteStationSmallMarkers.clear()

            mInFavoriteStationSmallMarkers.addAll(mStationMarkersSmall.filter {
                val station = it.tag as StationOnMap
                favoriteStations!!.contains(station.id)
            })

            mInFavoriteStationSmallMarkers.forEach { it.isVisible = false }

            mFavoriteStationMarkers.forEach { it.remove() }
            mFavoriteStationMarkers.clear()
            val icon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station_favorite)
            val favoritesMarkers = mAllStations.filter { favoriteStations!!.contains(it.id) }.mapNotNull {
                val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.getTitle()).icon(icon).zIndex(1.0f).anchor(.5f, .5f))
                marker?.tag = it
                marker
            }.toMutableList()

            mFavoriteStationMarkers = favoritesMarkers
        } else {
            mFavoriteStationMarkers.forEach { it.remove() }
            mFavoriteStationMarkers.clear()
            mInFavoriteStationSmallMarkers.clear()
        }
        setVisibleStationSmall(mShowSmall)
        checkZoom()
    }

    fun clearRoutes() {
        mRoutesOnMap?.forEach { it.remove() }
        mRouteOnMap = null
    }


    private fun getRoute(stations: List<StationOnMap>?, @ColorRes color: Int): PolylineOptions? {
        if (stations.isNullOrEmpty()) return null
        val line = PolylineOptions()
        val points = stations.map { LatLng(it.lat, it.lon) }
        line.addAll(points)
        val d = mActivity.resources.displayMetrics.density
        line.width(2 * d)
        val clr = ContextCompat.getColor(mActivity, color)
        line.color(clr)
        line.zIndex(1f)
        return line
    }

    fun showRoute(route: Route) {
        if (mRouteOnMap == route.name) return
        mRoutesOnMap?.forEach { it.remove() }
        mRouteOnMap = null

        if (!::mMap.isInitialized)
            return

        if (route.allStations != null) {
            val lines = mutableListOf<Polyline>()
            val line1 = getRoute(route.allStations, R.color.route)
            val line2 = getRoute(route.allStationsReverse, R.color.routeReverse)
            if (line2 != null) lines.add(mMap.addPolyline(line2))
            if (line1 != null) lines.add(mMap.addPolyline(line1))
            if (lines.isNotEmpty()) mRoutesOnMap = lines.toList()
            mRouteOnMap = route.name
        } else {
            val oneRoute = getRoute(route.stations, R.color.route)
            mRoutesOnMap =  if (oneRoute!=null) listOf(mMap.addPolyline(oneRoute)) else null
        }

    }


    @SuppressLint("MissingPermission")
    fun goToMyLocation() {
        if (!::mMap.isInitialized) return
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.continueWith {
            val location = it.result
            if (location != null && it.isSuccessful) {
                val latLng = LatLng(location.latitude, location.longitude)
                if (location.latitude == .0) return@continueWith
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            }
        }
        checkZoom()
    }


    private fun setTrafficJam(show: Boolean) {
        mTraffic = show
        if (::mMap.isInitialized) mMap.isTrafficEnabled = show
    }

    fun getInstanceStateBundle(outState: Bundle?) {
        if (::mMap.isInitialized && outState != null) {
            val center = mMap.projection.visibleRegion.latLngBounds.center
            outState.putDouble("lat", center.latitude)
            outState.putDouble("lon", center.longitude)
            outState.putFloat("zoom", mMap.cameraPosition.zoom)
        }
    }


    fun restoreInstanceStateBundle(savedInstanceState: Bundle?) {
        initPosition = null
        if (savedInstanceState != null) {
            val lat = savedInstanceState.getDouble("lat")
            val lon = savedInstanceState.getDouble("lon")
            val zoom = savedInstanceState.getFloat("zoom")
            if (lat != .0 && lon != .0 && zoom != 0f) {
                if (::mMap.isInitialized)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), zoom))
                else {
                    initPosition = LatLng(lat, lon)
                    initZoom = zoom
                }
            }
        }
    }

    fun zoomIn() {
        if (!::mMap.isInitialized) return
        mMap.animateCamera(CameraUpdateFactory.zoomIn())
    }

    fun zoomOut() {
        if (!::mMap.isInitialized) return
        mMap.animateCamera(CameraUpdateFactory.zoomOut())
    }

    fun goToStation(station: StationOnMap) {
        if (station.lat <= 0) return
        val latLng = LatLng(station.lat - 0.0006, station.lon)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
    }
}
