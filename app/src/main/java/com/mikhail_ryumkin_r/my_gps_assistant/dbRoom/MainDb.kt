package com.mikhail_ryumkin_r.my_gps_assistant.dbRoom

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database (entities = [TrackItemChet::class], version = 1, exportSchema = false)
abstract class MainDb : RoomDatabase() {

    abstract fun getDao(): DaoInterface
    companion object{
        @Volatile
        var INSTANCE: MainDb? = null
        fun getDatabase(context: Context): MainDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDb::class.java,
                    "GpsAssistant.db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}