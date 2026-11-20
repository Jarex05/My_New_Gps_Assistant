package com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel

import com.mikhail_ryumkin_r.my_gps_assistant.location.locationService.MyLocationService
import org.osmdroid.util.GeoPoint
import java.io.Serializable

data class LocationModel(
    val speed: Float = 0.0f,
    val distance: Float = 0.0f,
    val kmDistance: Int = 0,
    val pkDistance: Int = 0,
    val geoPointsTrack: ArrayList<GeoPoint>,
    val listOgrFakt: MutableList<MyLocationService.ListOgrFakt1> = mutableListOf(),
    val tvKmPkList: String = ""
) : Serializable