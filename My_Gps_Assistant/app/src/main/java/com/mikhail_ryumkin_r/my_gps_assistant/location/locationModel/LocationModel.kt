package com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel

import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import java.io.Serializable

data class LocationModel(
    val speed: Float = 0.0f,
    val distance: Float = 0.0f,
    val kmDistance: Int = 0,
    val pkDistance: Int = 0,
    val geoPointsTrack: ArrayList<GeoPoint>,
) : Serializable