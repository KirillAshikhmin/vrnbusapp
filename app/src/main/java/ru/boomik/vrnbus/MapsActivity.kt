package ru.boomik.vrnbus

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.method.LinkMovementMethod
import android.transition.Fade
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import ru.boomik.vrnbus.dialogs.SelectBusDialog
import ru.boomik.vrnbus.dialogs.SelectStationDialog
import ru.boomik.vrnbus.dialogs.StationInfoDialog
import ru.boomik.vrnbus.dialogs.progressDialog
import ru.boomik.vrnbus.managers.*
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.color
import ru.boomik.vrnbus.utils.requestPermission
import ru.boomik.vrnbus.views.RectangleContourProgressView
import ru.codemybrainsout.ratingdialog.RatingDialog
import java.util.*
import android.widget.TextView as WidgetTextView


class MapsActivity : AppCompatActivity() {

    private lateinit var progress: RectangleContourProgressView
    private var _loaded: Boolean = false
    private var _loading: Boolean = false
    private lateinit var menuManager: MenuManager
    private lateinit var mapManager: MapManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var mActivityView: ConstraintLayout

    private var mActive: Boolean = true
    private var mReadyCalled: Boolean = false
    private lateinit var mInsets: WindowInsetsCompat

    private lateinit var timer: Timer


    override fun onCreate(savedInstanceState: Bundle?) {
        //region SetupView
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.enterTransition = Fade()

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)

        DataBus.unsubscribeAll()

        settingsManager = SettingsManager
        settingsManager.initialize(this)
        AnalyticsManager.initByActivity(this, settingsManager.getBool(Consts.SETTINGS_ANALYTICS))

        setContentView(R.layout.activity_drawer)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val container = findViewById<ConstraintLayout>(R.id.container)
        val activityView = findViewById<ConstraintLayout>(R.id.activityView)
        val zoomButtons = findViewById<LinearLayout>(R.id.zoomButtons)
        val myLocation = findViewById<FloatingActionButton>(R.id.myLocation)
        val stationsButton = findViewById<FloatingActionButton>(R.id.stationsButton)
        val busButton = findViewById<FloatingActionButton>(R.id.busButton)
        val plus = findViewById<FloatingActionButton>(R.id.plus)
        val minus = findViewById<FloatingActionButton>(R.id.minus)
        val appVersion = findViewById<WidgetTextView>(R.id.app_version)
        val osmCopyright = findViewById<WidgetTextView>(R.id.osmCopyright)
        val fragmentParent = findViewById<FrameLayout>(R.id.fragmentParent)
        val menu = findViewById<FloatingActionButton>(R.id.menu)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        progress = findViewById(R.id.progress)

        mActivityView = activityView

        val showOsm = SettingsManager.getBool(Consts.SETTINGS_OSM)
        zoomButtons.visibility = if (settingsManager.getBool(Consts.SETTINGS_ZOOM)) View.VISIBLE else View.GONE
        appVersion.text = "Версия " + getVersionString()


        osmCopyright.text = HtmlCompat.fromHtml(getString(R.string.osm_copyright), HtmlCompat.FROM_HTML_MODE_COMPACT)
        osmCopyright.movementMethod = LinkMovementMethod.getInstance()

        osmCopyright.visibility = if (showOsm) View.VISIBLE else View.GONE

        // настройка поведения нижнего экрана
        //   bottomSheetBehavior = BottomSheetBehavior.from<View>(stationView)
        //     bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        if (supportFragmentManager.backStackEntryCount > 0) fragmentParent.setBackgroundColor(R.color.background.color(this))

        mapManager = MapManager(this, mapFragment)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#40111111")
            window.navigationBarColor = Color.parseColor("#40111111")

            ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
                val params = v.layoutParams as ViewGroup.MarginLayoutParams
                val d = resources.displayMetrics.density
                mInsets = insets
                params.leftMargin = insets.systemWindowInsetLeft
                params.topMargin = insets.systemWindowInsetTop
                params.rightMargin = insets.systemWindowInsetRight
                params.bottomMargin = insets.systemWindowInsetBottom
                appVersion.setPadding((16 * d).toInt(), 0, 0, (insets.systemWindowInsetBottom + 16 * d).toInt())
                val rect = Rect(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
                mapManager.padding = rect
                fragmentParent.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)

                MainScope().launch {
                    delay(1000)
                    if (!showWhatsNew(this@MapsActivity, insets)) onPrepareForReady()
                }

                FaveManager.initialize(this, mInsets)
                insets.consumeSystemWindowInsets()

            }
        } else {
            mInsets = WindowInsetsCompat(null)
            fragmentParent.setPadding(0, resources.getDimension(R.dimen.activity_vertical_margin).toInt(), 0, 0)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val count = supportFragmentManager.backStackEntryCount
            val colorFill = R.color.background.color(this)
            val colorTransparent = android.R.color.transparent.color(this)

            val colorFrom = if (count > 0) colorTransparent else colorFill
            val colorTo = if (count == 0) colorTransparent else colorFill
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
            colorAnimation.duration = 250 // milliseconds
            colorAnimation.addUpdateListener {
                fragmentParent.setBackgroundColor(it.animatedValue as Int)
            }
            colorAnimation.start()
        }

        menu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START) else drawerLayout.openDrawer(GravityCompat.START)
        }

        myLocation.setOnClickListener {
            goToMyLocation()
        }


        busButton.setOnClickListener {
            SelectBusDialog.show(this, mRoutes, mInsets) { routes ->
                onQuerySubmit(routes)
            }
        }
        stationsButton.setOnClickListener {
            SelectStationDialog.show(this, mInsets) { station ->
                mapManager.goToStation(station)
                DataBus.sendEvent(DataBus.StationClick, station)
            }
        }

        //endregion

        mapManager.subscribeReady {
        }

        menuManager = MenuManager(this)
        menuManager.initialize(navView)

        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                updateBuses()
            }
        }, 0, 30 * 1000)
        restoreInstanceState(savedInstanceState)

        plus.setOnClickListener {
            mapManager.zoomIn()
            AnalyticsManager.logEvent("zoom", "in")
        }
        minus.setOnClickListener {
            mapManager.zoomOut()
            AnalyticsManager.logEvent("zoom", "out")
        }

        DataBus.subscribe<Bus>(DataBus.BusClick) { onBusClicked(it.data) }
        DataBus.subscribe<StationOnMap>(DataBus.StationClick) { onStationClicked(it.data) }
        DataBus.subscribe<Boolean>(Consts.SETTINGS_ZOOM) { zoomButtons.visibility = if (it.data) View.VISIBLE else View.GONE }
        DataBus.subscribe<String>(Consts.SETTINGS_NIGHT) { setUiMode(it.data, true) }
        DataBus.subscribe<String>(DataBus.ResetRoutes) { resetRoutes(it.data) }
        DataBus.subscribe<String>(DataBus.AddRoutes) { addRoutes(it.data) }
        settingsManager.loadPreferences()

        val routes = SettingsManager.getString("routes")
        if (routes != null && routes!="*") {
            mRoutes = routes
            updateBuses()
        }
    }


    private fun onReady() {
        val ratingDialog = RatingDialog.Builder(this)
                .threshold(4f)
                .session(7)
                .onRatingBarFormSumbit { feedback, rating ->
                    AnalyticsManager.logEvent("rating_feedback", rating.toString(), mapOf(Pair<String,String>("feedback", feedback)))
                    AnalyticsManager.logPreference("rating", rating.toString())
                }
                .onRatingChanged { rating,  _ ->
                    AnalyticsManager.logEvent("rating_feedback", rating.toString())
                    AnalyticsManager.logPreference("rating", rating.toString())
                }
                .build()
        ratingDialog.show()
    }



    private fun displaySnackBarWithBottomMargin(snackbar: Snackbar, leftMargin: Int, rightMargin: Int, marginBottom: Int) {
        val snackBarView = snackbar.view
        val params = snackBarView.layoutParams as (FrameLayout.LayoutParams)

        params.setMargins(params.leftMargin + leftMargin,
                params.topMargin,
                params.rightMargin + rightMargin,
                params.bottomMargin + marginBottom)

        snackBarView.layoutParams = params

        Handler().postDelayed({
            snackbar.show()
        }, 2000)
    }

    private fun addRoutes(route: String) {
        val searchRoutes = if (mRoutes.isNotEmpty()) {
            val routesString = "$mRoutes, $route"
            val routes = routesString.split(',').asSequence().distinct().map { it.trim() }.toList()
            routes.joinToString()
        } else route
        onQuerySubmit(searchRoutes)
    }

    private fun resetRoutes(route: String) {
        onQuerySubmit(route)
    }

    private fun setUiMode(data: String?, needRecreate: Boolean = false) {
        val mode = when (data?.toIntOrNull()) {
            0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            1 -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            2 -> AppCompatDelegate.MODE_NIGHT_NO
            3 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppCompatDelegate.setDefaultNightMode(mode)
        if (needRecreate) {
            recreate()
        }
    }

    private fun getVersionString(): CharSequence? {
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            val versionCode = PackageInfoCompat.getLongVersionCode(pInfo)
            return "$version ($versionCode)"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun onStationClicked(stationOnMap: StationOnMap) {
        StationInfoDialog.show(this, mInsets, stationOnMap)
    }


    private var mAutoUpdateRoutes: Boolean = true
    private var mRoutes: String = ""

    private fun updateBuses() {

        val stationId = DataManager.searchStationId
        if (!mAutoUpdateRoutes || (mRoutes.isBlank() && stationId > 0) || !mActive) return
        if (stationId > 0) runOnUiThread { showBusesFromStation(stationId) }
        else if (mRoutes.isNotBlank()) runOnUiThread { showBuses(mRoutes) }
    }


    override fun onResume() {
        super.onResume()

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
        mActive = true
        updateBuses()

    }


    override fun onStart() {
        super.onStart()

        if (_loaded || _loading) return
        loadData()
    }

    fun loadData() {
        _loading = true
        scope.launch {
            var dialog: AlertDialog? = null
            try {
                dialog = try {
                   progressDialog(this@MapsActivity)
                } catch (t: Throwable) {null}

                DataManager.loadData(scope)

                _loaded = DataManager.loaded
            } catch (e: Throwable) {
                _loaded = false
                // if one of the long operation throw, this should be called once, even if multiple long operations failed.
                Log.e("something went wrong", e)

                android.app.AlertDialog.Builder(this@MapsActivity)
                    .setCancelable(false)
                    .setTitle("Ошибка загрузки данных")
                    .setMessage("Не удалось загрузить данные о маршрутах и остановках. Повторить?")
                    .setPositiveButton("Повторить") { _, _ -> loadData() }
                    .setNegativeButton("Выход") { _, _ -> this@MapsActivity.finish() }

            }

            try {

                dialog?.dismiss()
                showBusStations()
                _loading = false
                onPrepareForReady()
            } catch (t: Throwable) {
                Log.e("load data error")
            }
        }
    }

    private fun onPrepareForReady() {
        if (mReadyCalled) return
        if (!_loaded || !::mInsets.isInitialized) return
        mReadyCalled=true
        onReady()
    }


    override fun onPause() {
        mActive = false
        val position = mapManager.getLastLocation()
        DataBus.sendEvent(DataBus.Settings, Pair(Consts.SETTINGS_LAT, position.first.latitude))
        DataBus.sendEvent(DataBus.Settings, Pair(Consts.SETTINGS_LNG, position.first.longitude))
        DataBus.sendEvent(DataBus.Settings, Pair(Consts.SETTINGS_LAST_ZOOM, position.second))
        super.onPause()
        // mapManager.pause()
    }


    private fun onBusClicked(bus: Bus) {
        Toast.makeText(this, "Маршрут: ${bus.bus.routeName}\n${bus.getSnippet()}", Toast.LENGTH_LONG).show()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        mapManager.getInstanceStateBundle(outState)
        outState.putString("routes", mRoutes)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        try {
            super.onRestoreInstanceState(savedInstanceState)
            restoreInstanceState(savedInstanceState)
        } catch (e: Throwable) {
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        mapManager.restoreInstanceStateBundle(savedInstanceState)
        val routes = savedInstanceState?.getString("routes")
        if (routes != null) {
            mRoutes = routes
            updateBuses()
        }
    }

    public override fun onDestroy() {
        FaveManager.destroy(this)
        super.onDestroy()
        timer.cancel()
        DataBus.unsubscribeAll()
        val map = supportFragmentManager.findFragmentById(R.id.map)
        if (map != null) supportFragmentManager.beginTransaction().remove(map).commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AnalyticsManager.logPermission(requestCode, permissions, grantResults)
        when (requestCode) {
            Consts.LOCATION_PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    goToMyLocation()
            }
        }
    }

    override fun onBackPressed() {

        val decorView = window.decorView as FrameLayout?
        if (decorView != null && decorView.childCount > 0) {
            val last = decorView.getChildAt(decorView.childCount - 1)
            if (last.tag == "dialog") {
                val t = Slide(Gravity.TOP)
                TransitionManager.beginDelayedTransition(decorView, t)
                decorView.removeView(last)
                return
            }
        }
        super.onBackPressed()
    }

    @SuppressLint("MissingPermission")
    private fun goToMyLocation() {
        if (requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, Consts.LOCATION_PERMISSION_REQUEST)) mapManager.goToMyLocation()
    }

    private fun onQuerySubmit(query: String) {
        mRoutes = query
        showBuses(query)
    }

    private fun showBusesFromStation(stationId: Int) {
        try {
            mapManager.clearRoutes()

            scope.async(Dispatchers.Main) {
                progress.startAnimate()
                /*val stationInfo = BusService.loadArrivalInfoAsync(stationId).await()
                if (stationInfo != null) {
                    val buses = stationInfo.buses
                    DataBus.sendEvent(DataBus.BusToMap, buses)
                }*/
                progress.stopAnimate()
            }

        } catch (exception: Throwable) {
            Log.e("Hm..", exception)
            progress.stopAnimate()
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private fun showBuses(q: String) {
        SettingsManager.setString("routes", q)
        DataManager.searchStationId = -1
        mAutoUpdateRoutes = true
        if (q.isEmpty()) {
            mapManager.clearBusesOnMap()
            mapManager.clearRoutes()
            return
        }
        try {
            progress.startAnimate()
            mapManager.clearRoutes()
            if (!q.contains(',') && q != "*") {

                scope.launch {
                    val route = BusService.loadRouteByNameAsync(q.trim())
                    if (route != null) runOnUiThread {
                        mapManager.showRoute(route)
                    }
                }
            }
            Toast.makeText(this, "Поиск маршрутов:\n$q", Toast.LENGTH_SHORT).show()
            scope.launch {
                val buses = BusService.loadBusInfoAsync(q)
                    if (buses != null) {
                        runOnUiThread {
                            if (buses.count() == 0)
                                Toast.makeText(this@MapsActivity, "Не найдено МТС на выбранных маршрутах", Toast.LENGTH_SHORT).show()
                            mapManager.showBusesOnMap(buses, clearRoute = false)
                        }
                    } else {
                        Toast.makeText(this@MapsActivity, "Ничего не нашлось", Toast.LENGTH_SHORT).show()
                        mapManager.clearBusesOnMap()
                    }
                    progress.stopAnimate()

            }
        } catch (exception: Throwable) {
            Log.e("Hm..", exception)
            progress.stopAnimate()
        }
    }

    private fun showBusStations() {

        try {
            scope.launch {
                delay(500)
                val data = DataManager.stations

                if (data!=null) {
                    val stations = data.map { s -> StationOnMap(s.title, s.id, s.latitude, s.longitude) }
                    runOnUiThread {
                        mapManager.showStations(stations)
                    }
                }
            }
        } catch (exception: Throwable) {
            Log.e("VrnBus", "Hm..", exception)
        }
    }
}
