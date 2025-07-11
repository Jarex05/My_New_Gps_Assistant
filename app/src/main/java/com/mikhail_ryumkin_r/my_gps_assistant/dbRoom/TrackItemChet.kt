package com.mikhail_ryumkin_r.my_gps_assistant.dbRoom

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity (tableName = "trackChet")
data class TrackItemChet(
    @PrimaryKey (autoGenerate = true)
    val id: Int?,
    @ColumnInfo (name = "title")
    var title: String,
    @ColumnInfo (name = "distance")
    val distance: String,
    @ColumnInfo (name = "geo_points")
    val geoPoints: String,
)
