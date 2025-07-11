package com.mikhail_ryumkin_r.my_gps_assistant

import android.app.Application
import android.util.Log
import com.mikhail_ryumkin_r.my_gps_assistant.dbRoom.MainDb

class MainApp : Application() {
    val database by lazy { MainDb.getDatabase(this) }
}