package com.mikhail_ryumkin_r.my_gps_assistant.dbRoom

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoInterface {
    @Insert
    suspend fun insertTrack(trackItemChet: TrackItemChet)
    @Query("SELECT * FROM TRACKCHET")
    fun getAllTracks(): Flow<List<TrackItemChet>>
    @Delete
    suspend fun deleteTrack(trackItemChet: TrackItemChet)
}