package com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel

import org.osmdroid.util.GeoPoint
import java.io.Serializable

data class LocationModel(
    val speed: Float = 0.0f,
    val distance: Float = 0.0f,
    val kmDistance: Int = 0,
    val pkDistance: Int = 0,
    val geoPointsTrack: ArrayList<GeoPoint>,
    val tvOgr15: String = "",
    val tvOgr25: String = "",
    val tvOgr40: String = "",
    val tvOgr50: String = "",
    val tvOgr55: String = "",
    val tvOgr60: String = "",
    val tvOgr65: String = "",
    val tvOgr70: String = "",
    val tvOgr75: String = "",
    val tvKmPk15: String = "",
    val tvKmPk25: String = "",
    val tvKmPk40: String = "",
    val tvKmPk50: String = "",
    val tvKmPk55: String = "",
    val tvKmPk60: String = "",
    val tvKmPk65: String = "",
    val tvKmPk70: String = "",
    val tvKmPk75: String = ""
) : Serializable