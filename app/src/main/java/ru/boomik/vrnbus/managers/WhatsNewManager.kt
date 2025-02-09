package ru.boomik.vrnbus.managers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import io.github.tonnyl.whatsnew.WhatsNew
import io.github.tonnyl.whatsnew.item.WhatsNewItem
import io.github.tonnyl.whatsnew.util.PresentationOption
import ru.boomik.vrnbus.BuildConfig
import ru.boomik.vrnbus.R


fun showWhatsNew(activity: AppCompatActivity, insets: WindowInsetsCompat) : Boolean {
    try {
        var nowVersionCode: Int
        activity.packageManager
                .getPackageInfo(activity.packageName, 0)
                .let {
                    nowVersionCode = it.versionCode
                }

        val whatsNew = when {
            nowVersionCode >= 40 -> showWhatsNewFor40(activity)
            nowVersionCode >= 32 -> showWhatsNewFor32(activity)
            nowVersionCode >= 18 -> showWhatsNewFor18(activity)
            nowVersionCode >= 16 -> showWhatsNewFor16(activity)
            nowVersionCode >= 14 -> showWhatsNewFor14(activity)
            nowVersionCode >= 13 -> showWhatsNewFor13(activity)
            nowVersionCode >= 12 -> showWhatsNewFor12(activity)
            nowVersionCode >= 11 -> showWhatsNewFor11(activity)
            else -> null
        }
        if (whatsNew != null) {
            with(whatsNew) {
                buttonText = activity.getString(R.string.cont)
                buttonTextColor = ContextCompat.getColor(activity, R.color.background)
                buttonBackground = ContextCompat.getColor(activity, R.color.textColor)
                itemTitleColor = ContextCompat.getColor(activity, R.color.textColor)
                itemContentColor = ContextCompat.getColor(activity, R.color.textColor)
                titleColor = ContextCompat.getColor(activity, R.color.textColor)
                titleText = activity.getString(R.string.whatsnew)
                presentationOption = PresentationOption.IF_NEEDED
                windowInsets = insets
            }
            return whatsNew.presentAutomatically(activity)
        }

    } catch (t: Throwable) {

    }
    return false
}


private fun showWhatsNewFor40(activity: AppCompatActivity): WhatsNew {
    return WhatsNew.newInstance(
            itemFromRes(R.string.ac_title, R.string.ac_desc, R.drawable.ic_ac, activity),
            itemFromRes(R.string.bort_title, R.string.bort_desc, R.drawable.ic_123, activity),
            itemFromRes(R.string.improves_40_title, R.string.improves_40_desc, R.drawable.ic_improve, activity),
            itemFromRes(R.string.fixes_40_title, R.string.fixes_40_desc, R.drawable.ic_bug, activity),
            itemFromRes(R.string.other_title, R.string.other_desc, R.drawable.ic_optimization, activity)
    )
}
private fun showWhatsNewFor32(activity: AppCompatActivity): WhatsNew {
    return WhatsNew.newInstance(
            itemFromRes(R.string.station_list_title, R.string.station_list_desc, R.drawable.ic_bus_station, activity),
            itemFromRes(R.string.favorite_route_title, R.string.favorite_route_desc, R.drawable.ic_favorite, activity),
            itemFromRes(R.string.fine_route_title, R.string.fine_route_desc, R.drawable.ic_route, activity),
            itemFromRes(R.string.cloud_title, R.string.cloud_desc, R.drawable.ic_cloud, activity),
            itemFromRes(R.string.other_title, R.string.other_desc, R.drawable.ic_optimization, activity)
    )
}


private fun showWhatsNewFor18(activity: AppCompatActivity): WhatsNew {
    return WhatsNew.newInstance(
            itemFromRes(R.string.more_info_title, R.string.more_info_desc, R.drawable.ic_list, activity),
            itemFromRes(R.string.select_route_title, R.string.select_route_desc, R.drawable.ic_star, activity),
            itemFromRes(R.string.old_bus_title, R.string.old_bus_desc, R.drawable.ic_bus, activity),
            itemFromRes(R.string.save_route_title, R.string.save_route_desc, R.drawable.ic_rotate, activity),
            itemFromRes(R.string.other_title, R.string.other_desc, R.drawable.ic_optimization, activity)
    )
}

private fun showWhatsNewFor16(activity: AppCompatActivity): WhatsNew {
    return WhatsNew.newInstance(
            itemFromRes(R.string.station_bug_title, R.string.station_bug_desc, R.drawable.ic_bus_stop, activity),
            itemFromRes(R.string.rotate_title, R.string.rotate_desc, R.drawable.ic_rotate, activity)
    )
}


private fun showWhatsNewFor14(activity: AppCompatActivity): WhatsNew {
    return WhatsNew.newInstance(
            itemFromRes(R.string.big_station_end_title, R.string.big_station_end_desc, R.drawable.ic_bus_stop, activity),
            itemFromRes(R.string.distance_title, R.string.distance_desc, R.drawable.ic_distance, activity),
            itemFromRes(R.string.other_title, R.string.other_desc, R.drawable.ic_optimization, activity)
    )
}

private fun showWhatsNewFor13(activity: AppCompatActivity): WhatsNew {
    return WhatsNew.newInstance(
            itemFromRes(R.string.kitkat_title, R.string.kitkat_desc, R.drawable.ic_android, activity),
            itemFromRes(R.string.big_station_title, R.string.big_station_desc, R.drawable.ic_bus_stop, activity),
            itemFromRes(R.string.other_title, R.string.other_desc, R.drawable.ic_optimization, activity)
    )
}

private fun showWhatsNewFor12(activity: AppCompatActivity): WhatsNew {
    return WhatsNew.newInstance(
            itemFromRes(R.string.map_title, R.string.map_desc, R.drawable.ic_map, activity),
            itemFromRes(R.string.bus_type_title, R.string.bus_type_desc, R.drawable.ic_bus, activity),
            itemFromRes(R.string.favorite_title, R.string.favorite_desc, R.drawable.ic_no_star, activity),
            itemFromRes(R.string.night_title, R.string.night_desc, R.drawable.ic_night, activity),
            itemFromRes(R.string.zoom_title, R.string.zoom_desc, R.drawable.ic_zoom, activity),
            itemFromRes(R.string.other_title, R.string.other_desc, R.drawable.ic_optimization, activity)
    )
}

private fun showWhatsNewFor11(activity: AppCompatActivity): WhatsNew {
    return WhatsNew.newInstance(
            itemFromRes(R.string.map_title, R.string.map_desc, R.drawable.ic_map, activity),
            itemFromRes(R.string.bus_type_title, R.string.bus_type_desc, R.drawable.ic_bus, activity),
            itemFromRes(R.string.favorite_title, R.string.favorite_desc, R.drawable.ic_no_star, activity),
            itemFromRes(R.string.night_title, R.string.night_desc, R.drawable.ic_night, activity),
            itemFromRes(R.string.zoom_title, R.string.zoom_desc, R.drawable.ic_zoom, activity),
            itemFromRes(R.string.other_title, R.string.other_desc, R.drawable.ic_optimization, activity)
    )
}

private fun itemFromRes(@StringRes title: Int, @StringRes desc: Int, @DrawableRes image: Int, activity: AppCompatActivity): WhatsNewItem {
    return WhatsNewItem(activity.getString(title), activity.getString(desc), image)
}
