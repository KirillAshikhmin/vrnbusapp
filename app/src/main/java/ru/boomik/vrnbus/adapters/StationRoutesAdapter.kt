package ru.boomik.vrnbus.adapters

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.dal.businessObjects.BusObject
import ru.boomik.vrnbus.managers.SettingsManager
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.BusType
import ru.boomik.vrnbus.utils.BusViewHolder
import ru.boomik.vrnbus.utils.color
import java.util.*
import kotlin.random.Random

class StationRoutesAdapter(private val context: Activity, BussList: List<Bus>) : BaseAdapter() {

    private var busesList: List<Bus> = BussList
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var favorites: List<String>?


    val small: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_bus_small)!!
    val medium: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_bus_middle)!!
    val big: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_bus_large)!!
    val trolleybus: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_trolleybus)!!
    val wheelchair: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_wheelchair)!!
    val ac: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_ac)!!

    init {
        favorites = SettingsManager.getStringArray(Consts.SETTINGS_FAVORITE_ROUTE)
        DataBus.subscribe<Pair<String, Boolean>>(DataBus.FavoriteRoute) {
            favorites = SettingsManager.getStringArray(Consts.SETTINGS_FAVORITE_ROUTE)
            notifyDataSetChanged()
        }

        val color  = R.color.textColor.color(context)

        small.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
        medium.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
        big.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
        trolleybus.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
        wheelchair.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
        ac.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
    }

    fun dataEquals(routes: String): Boolean {
        if (routes.isBlank()) return true
        val dataRoutes = busesList.asSequence().map { it.bus.routeName }.map { it.trim() }.distinct().toList()
        val routesList = routes.split(',').asSequence().map { it.trim() }.distinct().toList()
        if (routesList.size != dataRoutes.size) return false
        return dataRoutes.firstOrNull { !routesList.contains(it) } == null
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val view: View?
        val vh: BusViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.bus_cell, parent, false)
            vh = BusViewHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as BusViewHolder
        }

        val bus = getItem(position) as Bus

        vh.tvTitle.text = bus.bus.routeName

        vh.tvAbsoluteTime.includeFontPadding = false
        vh.tvContent.includeFontPadding = false

        val time = bus.timeLeft.toInt()
        val timeToArrival = bus.timeToArrival

        val arrival = bus.arrivalTime
        if (arrival != null) {
            val absoluteTime = DateFormat.format("kk:mm", arrival).toString()
            vh.tvAbsoluteTime.text = "${context.getString(R.string.arrival_at)}$absoluteTime"
        } else vh.tvAbsoluteTime.text = null

        if (bus.bus.lowFloor) {
            vh.ivLowFloor.setImageDrawable(wheelchair)
            vh.ivLowFloor.visibility = View.VISIBLE
        } else {
            vh.ivLowFloor.setImageDrawable(null)
            vh.ivLowFloor.visibility = View.GONE
        }

        if (bus.bus.hasConditioning) {
            vh.ivAc.setImageDrawable(ac)
            vh.ivAc.visibility = View.VISIBLE
        } else {
            vh.ivAc.setImageDrawable(null)
            vh.ivAc.visibility = View.GONE
        }

        val icon = when (bus.bus.busType) {
            BusObject.BusType.Small -> small
            BusObject.BusType.Medium -> medium
            BusObject.BusType.Big -> big
            BusObject.BusType.Trolleybus -> trolleybus
            BusObject.BusType.Unknown -> null
            else -> big
        }

        vh.ivBusType.visibility = if (icon==null) View.GONE else View.VISIBLE
        vh.ivBusType.setImageDrawable(icon)
        vh.ivBusType.tag=bus.bus.busType

        (vh.tvTitle.tag as? ValueAnimator)?.cancel()

        if (time >= 1000) {
            vh.tvContent.text = null
            vh.tvContent.tag = null
        } else {
            vh.tvContent.tag = timeToArrival
            updateTimeLeft(vh.tvContent)
            val anim = ValueAnimator.ofInt(30)
            anim.addUpdateListener { updateTimeLeft(vh.tvContent) }
            anim.duration = 30000
            anim.start()
            vh.tvTitle.tag = anim
        }

        var inFavorite = favorites?.contains(bus.bus.routeName) ?: false
        vh.btnFavorite.setImageResource(if (inFavorite) R.drawable.ic_star else R.drawable.ic_no_star)

        vh.btnFavorite.setOnClickListener {
            inFavorite=!inFavorite
            DataBus.sendEvent(DataBus.FavoriteRoute, Pair(bus.bus.routeName, inFavorite))
            vh.btnFavorite.setImageResource(if (inFavorite) R.drawable.ic_star else R.drawable.ic_no_star)
        }

        vh.btnFavorite.isFocusable = false

        return view
    }

    private fun updateTimeLeft(tvContent: TextView) {

        val timeString: String
        val timeToArrival = tvContent.tag as? Long
        if (timeToArrival == null) {
            tvContent.text = ""
            return
        }
        val timeLeft = timeToArrival - System.currentTimeMillis()
        if (timeLeft <= 10 * 1000) {
            tvContent.text = "Ждем"
            return
        }
        val cal = Calendar.getInstance()
        cal.clear()
        cal.add(Calendar.MILLISECOND, timeLeft.toInt())
        var min = cal.get(Calendar.MINUTE)
        val sec = cal.get(Calendar.SECOND)
        if (min > 1 && sec > 30) min++
        timeString = if (min < 1 && sec < 30) "Прибывает"
        else if (min < 1) "< 1 мин"
        else "$min мин"

        tvContent.text = timeString
    }

    override fun getItem(position: Int): Any {
        return busesList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return busesList.size
    }

    fun setRoutes(routes: List<Bus>) {
        busesList = routes
        notifyDataSetChanged()
    }
}
