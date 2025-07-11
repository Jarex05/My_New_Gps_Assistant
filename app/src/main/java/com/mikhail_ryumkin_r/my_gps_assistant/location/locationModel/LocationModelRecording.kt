package com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel

import org.osmdroid.util.GeoPoint
import java.io.Serializable

data class LocationModelRecording(
    val speedRecording: Float = 0.0f,
    val sbL: StringBuilder,
    val distance1Recording: Float = 0.0f,
    val distance2Recording: Float = 0.0f,
    val geoPointsList: ArrayList<GeoPoint>,
    val kmDistanceRecording: Int = 0,
    val pkDistanceRecording: Int = 0,
) : Serializable
