package com.mikhail_ryumkin_r.my_gps_assistant.location.locationService

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.mikhail_ryumkin_r.my_gps_assistant.MainActivity
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.db.redacktor.MyDbManagerRedacktor
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.RecordingARouteFragment.Companion.LOC_MODEL_INTENT_FRAGMENT_MINUS
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.RecordingARouteFragment.Companion.LOC_MODEL_INTENT_FRAGMENT_PLUS
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel.FragmentModelMinus
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel.FragmentModelPlus
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel.LocationModelRecording
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint

@Suppress("DEPRECATION")
class MyLocationServiceRecording : Service() {
    private var sbL = StringBuilder()
    private var distance1Recording: Float = 0.0f
    private var distance2Recording: Float = 0.0f
    private var distance3Recording: Float = 0.0f

    private var isRedacktorMinusChet = true
    private var isRedacktorPlusChet = true

    private var isRedaktorMinusNechet = true
    private var isRedaktorPlusNechet = true
    private lateinit var geoPointsList: ArrayList<GeoPoint>
    private var isDebag = true

    var kmDistance: Int = 0
    var pkDistance: Int = 0

    private var faktStartKm: Int = 0
    private var faktFinishKm: Int = 0

    private var lastLocationRecording: Location? = null
    private lateinit var locProviderRecording: FusedLocationProviderClient
    private lateinit var locRequestRecording: LocationRequest
    private lateinit var myDbManagerRedacktor: MyDbManagerRedacktor
    private var titleStartRedacktor: Int = 0
    private var piketStartRedacktor: Int = 0
    private var faktNachKmRedacktor: Int = 0
    private var piketNachKmRedacktor: Int = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val receiverPlus = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LOC_MODEL_INTENT_FRAGMENT_PLUS){
                val fragmentModelPlus = intent.getSerializableExtra(LOC_MODEL_INTENT_FRAGMENT_PLUS) as FragmentModelPlus
                distance1Recording += fragmentModelPlus.savePlus
                Log.d("MyLog30", "savePlus: $distance1Recording")
            }
        }
    }

    private fun registerLocReceiverPlus(){
        val locFilterPlus = IntentFilter(LOC_MODEL_INTENT_FRAGMENT_PLUS)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiverPlus, locFilterPlus)
    }

    private val receiverMinus = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LOC_MODEL_INTENT_FRAGMENT_MINUS){
                val fragmentModelMinus = intent.getSerializableExtra(LOC_MODEL_INTENT_FRAGMENT_MINUS) as FragmentModelMinus
                distance1Recording -= fragmentModelMinus.saveMinus
                Log.d("MyLog30", "saveMinus: $distance1Recording")
            }
        }
    }

    private fun registerLocReceiverMinus(){
        val locFilterMinus = IntentFilter(LOC_MODEL_INTENT_FRAGMENT_MINUS)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiverMinus, locFilterMinus)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        myDbManagerRedacktor = MyDbManagerRedacktor(applicationContext)
        distance1Recording = SharedPref.Companion.getValueKmAddd(baseContext).toFloat()
        distance2Recording = SharedPref.Companion.getValueKmAddd(baseContext).toFloat()
        distance3Recording = SharedPref.Companion.getValueKmAddd(baseContext).toFloat()
        registerLocReceiverPlus()
        registerLocReceiverMinus()
        startNotificationRecording()
        startLocationUpdatesRecording()
        isRunningRecording = true
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        geoPointsList = ArrayList()
        initLocationRecording()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunningRecording = false
        locProviderRecording.removeLocationUpdates(locCallbackRecording)
    }

    private val locCallbackRecording = object : LocationCallback() {
        override fun onLocationResult(lResult: LocationResult) {
            super.onLocationResult(lResult)
            val currentLocationRecording = lResult.lastLocation
            if (lastLocationRecording != null && currentLocationRecording != null) {
                if (currentLocationRecording.speed > 0.4 || isDebag){

                    val v = SharedPref.Companion.getValueRecording(baseContext)

                    if (v != 0) {
                        distance1Recording -= lastLocationRecording?.distanceTo(currentLocationRecording)!!
                        distance2Recording -= lastLocationRecording?.distanceTo(currentLocationRecording)!!
                        distance3Recording -= lastLocationRecording?.distanceTo(currentLocationRecording)!!

                        CoroutineScope(Dispatchers.IO).launch {
                            calculationKmNechet()
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            myDbManagerRedacktor = MyDbManagerRedacktor(applicationContext)
                            myDbManagerRedacktor.openDb()
                            val dataListRedacktor = myDbManagerRedacktor.readDbDataRedacktorNechet()
                            for (item in dataListRedacktor){
                                titleStartRedacktor = item.startNechet
                                piketStartRedacktor = item.picketStartNechet

                                // Начало расчёта начала отнимания или прибавления метража по киллометро

                                var x = 9999999
                                var kmx = 10000
                                val pkx = 10
                                while (x > 0){
                                    x -= 1000
                                    kmx -= 1

                                    if (titleStartRedacktor == kmx && piketStartRedacktor == pkx){
                                        faktNachKmRedacktor = x
                                        piketNachKmRedacktor = pkx
                                    }
                                }

                                var x1 = 9999899
                                var kmx1 = 10000
                                val pkx1 = 9
                                while (x1 > 0){
                                    x1 -= 1000
                                    kmx1 -= 1

                                    if (titleStartRedacktor == kmx1 && piketStartRedacktor == pkx1){
                                        faktNachKmRedacktor = x1
                                        piketNachKmRedacktor = pkx1
                                    }
                                }

                                var x2 = 9999799
                                var kmx2 = 10000
                                val pkx2 = 8
                                while (x2 > 0){
                                    x2 -= 1000
                                    kmx2 -= 1

                                    if (titleStartRedacktor == kmx2 && piketStartRedacktor == pkx2){
                                        faktNachKmRedacktor = x2
                                        piketNachKmRedacktor = pkx2
                                    }
                                }

                                var x3 = 9999699
                                var kmx3 = 10000
                                val pkx3 = 7
                                while (x3 > 0){
                                    x3 -= 1000
                                    kmx3 -= 1

                                    if (titleStartRedacktor == kmx3 && piketStartRedacktor == pkx3){
                                        faktNachKmRedacktor = x3
                                        piketNachKmRedacktor = pkx3
                                    }
                                }

                                var x4 = 9999599
                                var kmx4 = 10000
                                val pkx4 = 6
                                while (x4 > 0){
                                    x4 -= 1000
                                    kmx4 -= 1

                                    if (titleStartRedacktor == kmx4 && piketStartRedacktor == pkx4){
                                        faktNachKmRedacktor = x4
                                        piketNachKmRedacktor = pkx4
                                    }
                                }

                                var x5 = 9999499
                                var kmx5 = 10000
                                val pkx5 = 5
                                while (x5 > 0){
                                    x5 -= 1000
                                    kmx5 -= 1

                                    if (titleStartRedacktor == kmx5 && piketStartRedacktor == pkx5){
                                        faktNachKmRedacktor = x5
                                        piketNachKmRedacktor = pkx5
                                    }
                                }

                                var x6 = 9999399
                                var kmx6 = 10000
                                val pkx6 = 4
                                while (x6 > 0){
                                    x6 -= 1000
                                    kmx6 -= 1

                                    if (titleStartRedacktor == kmx6 && piketStartRedacktor == pkx6){
                                        faktNachKmRedacktor = x6
                                        piketNachKmRedacktor = pkx6
                                    }
                                }

                                var x7 = 9999299
                                var kmx7 = 10000
                                val pkx7 = 3
                                while (x7 > 0){
                                    x7 -= 1000
                                    kmx7 -= 1

                                    if (titleStartRedacktor == kmx7 && piketStartRedacktor == pkx7){
                                        faktNachKmRedacktor = x7
                                        piketNachKmRedacktor = pkx7
                                    }
                                }

                                var x8 = 9999199
                                var kmx8 = 10000
                                val pkx8 = 2
                                while (x8 > 0){
                                    x8 -= 1000
                                    kmx8 -= 1

                                    if (titleStartRedacktor == kmx8 && piketStartRedacktor == pkx8){
                                        faktNachKmRedacktor = x8
                                        piketNachKmRedacktor = pkx8
                                    }
                                }

                                var x9 = 9999099
                                var kmx9 = 10000
                                val pkx9 = 1
                                while (x9 > 0){
                                    x9 -= 1000
                                    kmx9 -= 1

                                    if (titleStartRedacktor == kmx9 && piketStartRedacktor == pkx9){
                                        faktNachKmRedacktor = x9
                                        piketNachKmRedacktor = pkx9
                                    }
                                }

                                // Конец расчёта начала отнимания или прибавления метража по киллометро

                                // Начало отнимания метража

                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (999 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 950 - 98 && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet999: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording999: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (949 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 900 - 98 && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet949: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording949: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (899 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 850 - 98 && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet899: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording899: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (849 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 800 - 98 && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet849: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording849: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (799 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 750 - 98 && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet799: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording799: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (749 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 700 - 98 && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet749: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording749: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (699 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 650 - 98 && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet699: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording699: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (649 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 600 - 98 && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet649: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording649: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (599 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 550 - 98 && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet599: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording599: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (549 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 500 - 98 && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet549: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording549: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (499 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 450 - 98 && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet499: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording499: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (449 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 400 - 98 && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet449: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording449: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (399 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 350 - 98 && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet399: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording399: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (349 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 300 - 98 && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet349: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording349: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (299 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 250 - 98 && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet299: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording299: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (249 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 200 - 98 && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet249: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording249: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (199 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 150 - 98 && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet199: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording199: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (149 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 100 - 98 && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet149: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording149: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (99 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor + 50 - 98 && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet99: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording99: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (item.minusNechet.isNotBlank() && distance2Recording <= faktNachKmRedacktor + (49 - item.minusNechet.toInt() / 20 - 100) && distance2Recording >= faktNachKmRedacktor - 98 && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusNechet49: $minusNechet")
                                    Log.d("MyLog160", "distance1Recording49: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                // Конец отнимания метража

                                // Начало прибавления метража

                                if (distance2Recording <= faktNachKmRedacktor + (999 - 99) && distance2Recording >= faktNachKmRedacktor + 950 - 98 && item.plusNechet.isNotBlank() && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet999: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording999: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + (949 - 99) && distance2Recording >= faktNachKmRedacktor + 900 - 98 && item.plusNechet.isNotBlank() && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet949: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording949: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + (899 - 99) && distance2Recording >= faktNachKmRedacktor + 850 - 98 && item.plusNechet.isNotBlank() && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet899: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording899: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + (849 - 99) && distance2Recording >= faktNachKmRedacktor + 800 - 98 && item.plusNechet.isNotBlank() && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet849: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording849: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + (799 - 99) && distance2Recording >= faktNachKmRedacktor + 750 - 98 && item.plusNechet.isNotBlank() && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet799: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording799: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + (749 - 99) && distance2Recording >= faktNachKmRedacktor + 700 - 98 && item.plusNechet.isNotBlank() && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet749: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording749: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + (699 - 99) && distance2Recording >= faktNachKmRedacktor + 650 - 98 && item.plusNechet.isNotBlank() && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet699: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording699: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + (649 - 99) && distance2Recording >= faktNachKmRedacktor + 600 - 98 && item.plusNechet.isNotBlank() && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet649: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording649: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + (599 - 99) && distance2Recording >= faktNachKmRedacktor + 550 - 98 && item.plusNechet.isNotBlank() && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet599: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording599: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + (549 - 99) && distance2Recording >= faktNachKmRedacktor + 500 - 98 && item.plusNechet.isNotBlank() && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet549: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording549: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + (499 - 99) && distance2Recording >= faktNachKmRedacktor + 450 - 98 && item.plusNechet.isNotBlank() && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet499: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording499: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + (449 - 99) && distance2Recording >= faktNachKmRedacktor + 400 - 98 && item.plusNechet.isNotBlank() && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet449: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording449: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + (399 - 99) && distance2Recording >= faktNachKmRedacktor + 350 - 98 && item.plusNechet.isNotBlank() && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet399: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording399: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + (349 - 99) && distance2Recording >= faktNachKmRedacktor + 300 - 98 && item.plusNechet.isNotBlank() && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet349: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording349: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + (299 - 99) && distance2Recording >= faktNachKmRedacktor + 250 - 98 && item.plusNechet.isNotBlank() && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet299: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording299: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + (249 - 99) && distance2Recording >= faktNachKmRedacktor + 200 - 98 && item.plusNechet.isNotBlank() && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet249: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording249: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + (199 - 99) && distance2Recording >= faktNachKmRedacktor + 150 - 98 && item.plusNechet.isNotBlank() && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet199: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording199: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + (149 - 99) && distance2Recording >= faktNachKmRedacktor + 100 - 98 && item.plusNechet.isNotBlank() && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet149: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording149: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + (99 - 99) && distance2Recording >= faktNachKmRedacktor + 50 - 98 && item.plusNechet.isNotBlank() && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet99: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording99: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + (49 - 99) && distance2Recording >= faktNachKmRedacktor - 98 && item.plusNechet.isNotBlank() && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusNechet49: $plusNechet")
                                    Log.d("MyLog160", "distance1Recording49: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                // Конец прибавления метража

                            }
                            myDbManagerRedacktor.closeDb()
                        }

                    } else {
                        distance1Recording += lastLocationRecording?.distanceTo(currentLocationRecording)!!
                        distance2Recording += lastLocationRecording?.distanceTo(currentLocationRecording)!!
                        distance3Recording += lastLocationRecording?.distanceTo(currentLocationRecording)!!

                        CoroutineScope(Dispatchers.IO).launch {
                            calculationKmChet()
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            myDbManagerRedacktor = MyDbManagerRedacktor(applicationContext)
                            myDbManagerRedacktor.openDb()
                            val dataListRedacktor = myDbManagerRedacktor.readDbDataRedacktorChet()
                            for (item in dataListRedacktor){
                                titleStartRedacktor = item.startChet
                                piketStartRedacktor = item.picketStartChet

                                // Начало расчёта начала отнимания или прибавления метража по киллометро

                                var z = 1001
                                var kms = 2
                                val pkc = 1

                                while (z < 9999999){
                                    z += 1000
                                    kms += 1

                                    if (titleStartRedacktor == kms && piketStartRedacktor == pkc){
                                        faktNachKmRedacktor = z
                                        piketNachKmRedacktor = pkc
                                    }
                                }

                                var z1 = 1101
                                var kms1 = 2
                                val pkc1 = 2

                                while (z1 < 9999999){
                                    z1 += 1000
                                    kms1 += 1

                                    if (titleStartRedacktor == kms1 && piketStartRedacktor == pkc1){
                                        faktNachKmRedacktor = z1
                                        piketNachKmRedacktor = pkc1
                                    }
                                }

                                var z2 = 1201
                                var kms2 = 2
                                val pkc2 = 3

                                while (z2 < 9999999){
                                    z2 += 1000
                                    kms2 += 1

                                    if (titleStartRedacktor == kms2 && piketStartRedacktor == pkc2){
                                        faktNachKmRedacktor = z2
                                        piketNachKmRedacktor = pkc2
                                    }
                                }

                                var z3 = 1301
                                var kms3 = 2
                                val pkc3 = 4

                                while (z3 < 9999999){
                                    z3 += 1000
                                    kms3 += 1

                                    if (titleStartRedacktor == kms3 && piketStartRedacktor == pkc3){
                                        faktNachKmRedacktor = z3
                                        piketNachKmRedacktor = pkc3
                                    }
                                }

                                var z4 = 1401
                                var kms4 = 2
                                val pkc4 = 5

                                while (z4 < 9999999){
                                    z4 += 1000
                                    kms4 += 1

                                    if (titleStartRedacktor == kms4 && piketStartRedacktor == pkc4){
                                        faktNachKmRedacktor = z4
                                        piketNachKmRedacktor = pkc4
                                    }
                                }

                                var z5 = 1501
                                var kms5 = 2
                                val pkc5 = 6

                                while (z5 < 9999999){
                                    z5 += 1000
                                    kms5 += 1

                                    if (titleStartRedacktor == kms5 && piketStartRedacktor == pkc5){
                                        faktNachKmRedacktor = z5
                                        piketNachKmRedacktor = pkc5
                                    }
                                }

                                var z6 = 1601
                                var kms6 = 2
                                val pkc6 = 7

                                while (z6 < 9999999){
                                    z6 += 1000
                                    kms6 += 1

                                    if (titleStartRedacktor == kms6 && piketStartRedacktor == pkc6){
                                        faktNachKmRedacktor = z6
                                        piketNachKmRedacktor = pkc6
                                    }
                                }

                                var z7 = 1701
                                var kms7 = 2
                                val pkc7 = 8

                                while (z7 < 9999999){
                                    z7 += 1000
                                    kms7 += 1

                                    if (titleStartRedacktor == kms7 && piketStartRedacktor == pkc7){
                                        faktNachKmRedacktor = z7
                                        piketNachKmRedacktor = pkc7
                                    }
                                }

                                var z8 = 1801
                                var kms8 = 2
                                val pkc8 = 9

                                while (z8 < 9999999){
                                    z8 += 1000
                                    kms8 += 1

                                    if (titleStartRedacktor == kms8 && piketStartRedacktor == pkc8){
                                        faktNachKmRedacktor = z8
                                        piketNachKmRedacktor = pkc8
                                    }
                                }

                                var z9 = 1901
                                var kms9 = 2
                                val pkc9 = 10

                                while (z9 < 9999999){
                                    z9 += 1000
                                    kms9 += 1

                                    if (titleStartRedacktor == kms9 && piketStartRedacktor == pkc9){
                                        faktNachKmRedacktor = z9
                                        piketNachKmRedacktor = pkc9
                                    }
                                }

                                // Конец расчёта начала отнимания или прибавления метража по киллометро

                                // Начало отнимания метража

                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 999 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 950 + 98 && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet999: $minusChet")
                                    Log.d("MyLog160", "distance1Recording999: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 949 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 900 + 98 && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet949: $minusChet")
                                    Log.d("MyLog160", "distance1Recording949: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 899 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 850 + 98 && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet899: $minusChet")
                                    Log.d("MyLog160", "distance1Recording899: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 849 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 800 + 98 && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet849: $minusChet")
                                    Log.d("MyLog160", "distance1Recording849: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 799 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 750 + 98 && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet799: $minusChet")
                                    Log.d("MyLog160", "distance1Recording799: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 749 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 700 + 98 && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet749: $minusChet")
                                    Log.d("MyLog160", "distance1Recording749: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 699 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 650 + 98 && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet699: $minusChet")
                                    Log.d("MyLog160", "distance1Recording699: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 649 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 600 + 98 && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet649: $minusChet")
                                    Log.d("MyLog160", "distance1Recording649: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 599 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 550 + 98 && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet599: $minusChet")
                                    Log.d("MyLog160", "distance1Recording599: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 549 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 500 + 98 && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet549: $minusChet")
                                    Log.d("MyLog160", "distance1Recording549: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 499 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 450 + 98 && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet499: $minusChet")
                                    Log.d("MyLog160", "distance1Recording499: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 449 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 400 + 98 && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet449: $minusChet")
                                    Log.d("MyLog160", "distance1Recording449: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 399 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 350 + 98 && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet399: $minusChet")
                                    Log.d("MyLog160", "distance1Recording399: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 349 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 300 + 98 && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet349: $minusChet")
                                    Log.d("MyLog160", "distance1Recording349: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 299 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 250 + 98 && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet299: $minusChet")
                                    Log.d("MyLog160", "distance1Recording299: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 249 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 200 + 98 && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet249: $minusChet")
                                    Log.d("MyLog160", "distance1Recording249: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 199 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 150 + 98 && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet199: $minusChet")
                                    Log.d("MyLog160", "distance1Recording199: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 149 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 100 + 98 && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet149: $minusChet")
                                    Log.d("MyLog160", "distance1Recording149: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 99 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor - 50 + 98 && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet99: $minusChet")
                                    Log.d("MyLog160", "distance1Recording99: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (item.minusChet.isNotBlank() && distance2Recording >= faktNachKmRedacktor - 49 + item.minusChet.toInt() / 20 + 100 && distance2Recording <= faktNachKmRedacktor + 98 && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "minusChet49: $minusChet")
                                    Log.d("MyLog160", "distance1Recording49: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorMinusChet = true
                                }
                                //---------------------------------------------------------------------------------------------------------------------------------------

                                // Конец отнимания метража

                                // Начало прибавления метража

                                if (distance2Recording >= faktNachKmRedacktor - 999 + 99 && distance2Recording <= faktNachKmRedacktor - 950 + 98 && item.plusChet.isNotBlank() && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet999: $plusChet")
                                    Log.d("MyLog160", "distance1Recording999: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 949 + 99 && distance2Recording <= faktNachKmRedacktor - 900 + 98 && item.plusChet.isNotBlank() && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet949: $plusChet")
                                    Log.d("MyLog160", "distance1Recording949: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 899 + 99 && distance2Recording <= faktNachKmRedacktor - 850 + 98 && item.plusChet.isNotBlank() && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet899: $plusChet")
                                    Log.d("MyLog160", "distance1Recording899: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 849 + 99 && distance2Recording <= faktNachKmRedacktor - 800 + 98 && item.plusChet.isNotBlank() && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet849: $plusChet")
                                    Log.d("MyLog160", "distance1Recording849: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 799 + 99 && distance2Recording <= faktNachKmRedacktor - 750 + 98 && item.plusChet.isNotBlank() && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet799: $plusChet")
                                    Log.d("MyLog160", "distance1Recording799: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 749 + 99 && distance2Recording <= faktNachKmRedacktor - 700 + 98 && item.plusChet.isNotBlank() && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet749: $plusChet")
                                    Log.d("MyLog160", "distance1Recording749: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 699 + 99 && distance2Recording <= faktNachKmRedacktor - 650 + 98 && item.plusChet.isNotBlank() && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet699: $plusChet")
                                    Log.d("MyLog160", "distance1Recording699: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 649 + 99 && distance2Recording <= faktNachKmRedacktor - 600 + 98 && item.plusChet.isNotBlank() && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet649: $plusChet")
                                    Log.d("MyLog160", "distance1Recording649: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 599 + 99 && distance2Recording <= faktNachKmRedacktor - 550 + 98 && item.plusChet.isNotBlank() && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet599: $plusChet")
                                    Log.d("MyLog160", "distance1Recording599: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 549 + 99 && distance2Recording <= faktNachKmRedacktor - 50 + 98 && item.plusChet.isNotBlank() && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet549: $plusChet")
                                    Log.d("MyLog160", "distance1Recording549: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 499 + 99 && distance2Recording <= faktNachKmRedacktor - 450 + 98 && item.plusChet.isNotBlank() && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet499: $plusChet")
                                    Log.d("MyLog160", "distance1Recording499: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 449 + 99 && distance2Recording <= faktNachKmRedacktor - 400 + 98 && item.plusChet.isNotBlank() && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet449: $plusChet")
                                    Log.d("MyLog160", "distance1Recording449: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 399 + 99 && distance2Recording <= faktNachKmRedacktor - 350 + 98 && item.plusChet.isNotBlank() && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet399: $plusChet")
                                    Log.d("MyLog160", "distance1Recording399: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 349 + 99 && distance2Recording <= faktNachKmRedacktor - 300 + 98 && item.plusChet.isNotBlank() && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet349: $plusChet")
                                    Log.d("MyLog160", "distance1Recording349: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 299 + 99 && distance2Recording <= faktNachKmRedacktor - 250 + 98 && item.plusChet.isNotBlank() && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet299: $plusChet")
                                    Log.d("MyLog160", "distance1Recording299: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 249 + 99 && distance2Recording <= faktNachKmRedacktor - 200 + 98 && item.plusChet.isNotBlank() && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet249: $plusChet")
                                    Log.d("MyLog160", "distance1Recording249: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 199 + 99 && distance2Recording <= faktNachKmRedacktor - 150 + 98 && item.plusChet.isNotBlank() && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet199: $plusChet")
                                    Log.d("MyLog160", "distance1Recording199: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 149 + 99 && distance2Recording <= faktNachKmRedacktor - 100 + 98 && item.plusChet.isNotBlank() && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet149: $plusChet")
                                    Log.d("MyLog160", "distance1Recording149: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 99 + 99 && distance2Recording <= faktNachKmRedacktor - 50 + 98 && item.plusChet.isNotBlank() && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet99: $plusChet")
                                    Log.d("MyLog160", "distance1Recording99: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 49 + 99 && distance2Recording <= faktNachKmRedacktor + 98 && item.plusChet.isNotBlank() && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    distance2Recording = distance1Recording
                                    Log.d("MyLog160", "plusChet49: $plusChet")
                                    Log.d("MyLog160", "distance1Recording49: $distance1Recording")
                                    Log.d("MyLog160", "distance2Recording: $distance2Recording")
                                    Log.d("MyLog160", "distance3Recording: $distance3Recording")
                                    isRedacktorPlusChet = true
                                }
                                //---------------------------------------------------------------------------------------------------------------------------------------

                                // Конец прибавления метража
                            }
                            myDbManagerRedacktor.closeDb()
                        }

                    }

                    geoPointsList.add(GeoPoint(currentLocationRecording.latitude, currentLocationRecording.longitude))

                    sbL.append("${currentLocationRecording.latitude}, ${currentLocationRecording.longitude}, $distance1Recording /")
//                if (mainDistanceChet > 0){
//                    sbL.append("${currentLocationChet.latitude}, ${currentLocationChet.longitude}, $distanceChet /")
//                } else {
//                    sbL.append("${currentLocationChet.latitude}, ${currentLocationChet.longitude}, 0000000.0 /")
//                }
//                if (currentLocationRecording.speed > 0.4) {
//
//                }

                }

                val locModelRecording = LocationModelRecording(
                    currentLocationRecording.speed,
                    sbL,
                    distance1Recording,
                    distance2Recording,
                    geoPointsList,
                    kmDistance,
                    pkDistance
                )
                sendLocDataRecording(locModelRecording)
                Log.d("MyLog1", "distanceRecording: $distance1Recording")
            }
            lastLocationRecording = currentLocationRecording
        }
    }

    private fun sendLocDataRecording(locModel: LocationModelRecording){
        val i = Intent(LOC_MODEL_INTENT_RECORDING)
        i.putExtra(LOC_MODEL_INTENT_RECORDING, locModel)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun startNotificationRecording(){

        val v = SharedPref.Companion.getValueRecording(baseContext)

        if (v != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nChanel = NotificationChannel(
                    CHANNEL_ID_RECORDING_NECHET,
                    "Location Service",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val nManager = getSystemService(NotificationManager::class.java) as NotificationManager
                nManager.createNotificationChannel(nChanel)
            }
            val nIntent = Intent(this, MainActivity::class.java)
            val pIntent = PendingIntent.getActivity(
                this,
                57,
                nIntent,
                PendingIntent.FLAG_MUTABLE
            )
            val notification = NotificationCompat.Builder(
                this,
                CHANNEL_ID_RECORDING_NECHET
            ).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Запись маршрута с Нечётным поездом")
                .setContentIntent(pIntent).build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(107, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                startForeground(107, notification)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nChanel = NotificationChannel(
                    CHANNEL_ID_RECORDING_CHET,
                    "Location Service",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val nManager = getSystemService(NotificationManager::class.java) as NotificationManager
                nManager.createNotificationChannel(nChanel)
            }
            val nIntent = Intent(this, MainActivity::class.java)
            val pIntent = PendingIntent.getActivity(
                this,
                56,
                nIntent,
                PendingIntent.FLAG_MUTABLE
            )
            val notification = NotificationCompat.Builder(
                this,
                CHANNEL_ID_RECORDING_CHET
            ).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Запись маршрута с Чётным поездом")
                .setContentIntent(pIntent).build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(106, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                startForeground(106, notification)
            }
        }
    }

    private fun initLocationRecording(){
        locRequestRecording = LocationRequest.create()
        locRequestRecording.interval = 1000
        locRequestRecording.fastestInterval = 1000
        locRequestRecording.priority = PRIORITY_HIGH_ACCURACY
        locProviderRecording = LocationServices.getFusedLocationProviderClient(baseContext)

    }

    private fun startLocationUpdatesRecording(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)

            return

        locProviderRecording.requestLocationUpdates(
            locRequestRecording,
            locCallbackRecording,
            Looper.myLooper()
        )
    }

    private suspend fun calculationKmNechet() = withContext(Dispatchers.IO){
        var q = 9999001
        var q1 = 9999101
        var q2 = 9999201
        var q3 = 9999301
        var q4 = 9999401
        var q5 = 9999501
        var q6 = 9999601
        var q7 = 9999701
        var q8 = 9999801
        var q9 = 9999901

        var w = 9999099
        var w1 = 9999199
        var w2 = 9999299
        var w3 = 9999399
        var w4 = 9999499
        var w5 = 9999599
        var w6 = 9999699
        var w7 = 9999799
        var w8 = 9999899
        var w9 = 9999999

        var kme = 10000
        var pke = 1

        while (w9 > 0) {
            q -= 1000
            q1 -= 1000
            q2 -= 1000
            q3 -= 1000
            q4 -= 1000
            q5 -= 1000
            q6 -= 1000
            q7 -= 1000
            q8 -= 1000
            q9 -= 1000

            w -= 1000
            w1 -= 1000
            w2 -= 1000
            w3 -= 1000
            w4 -= 1000
            w5 -= 1000
            w6 -= 1000
            w7 -= 1000
            w8 -= 1000
            w9 -= 1000

            kme -= 1

            if (distance1Recording <= w9 && distance1Recording >= q9){
                pke = 10
                kmDistance = kme
                pkDistance = pke

                faktStartKm = w9
                faktFinishKm = q9
            }

            if (distance1Recording <= w8 && distance1Recording >= q8){
                pke = 9
                kmDistance = kme
                pkDistance = pke

                faktStartKm = w8
                faktFinishKm = q8
            }

            if (distance1Recording <= w7 && distance1Recording >= q7){
                pke = 8
                kmDistance = kme
                pkDistance = pke

                faktStartKm = w7
                faktFinishKm = q7
            }

            if (distance1Recording <= w6 && distance1Recording >= q6){
                pke = 7
                kmDistance = kme
                pkDistance = pke

                faktStartKm = w6
                faktFinishKm = q6
            }

            if (distance1Recording <= w5 && distance1Recording >= q5){
                pke = 6
                kmDistance = kme
                pkDistance = pke

                faktStartKm = w5
                faktFinishKm = q5
            }

            if (distance1Recording <= w4 && distance1Recording >= q4){
                pke = 5
                kmDistance = kme
                pkDistance = pke

                faktStartKm = w4
                faktFinishKm = q4
            }

            if (distance1Recording <= w3 && distance1Recording >= q3){
                pke = 4
                kmDistance = kme
                pkDistance = pke

                faktStartKm = w3
                faktFinishKm = q3
            }

            if (distance1Recording <= w2 && distance1Recording >= q2){
                pke = 3
                kmDistance = kme
                pkDistance = pke

                faktStartKm = w2
                faktFinishKm = q2
            }

            if (distance1Recording <= w1 && distance1Recording >= q1){
                pke = 2
                kmDistance = kme
                pkDistance = pke

                faktStartKm = w1
                faktFinishKm = q1
            }

            if (distance1Recording <= w && distance1Recording >= q){
                pke = 1
                kmDistance = kme
                pkDistance = pke

                faktStartKm = w
                faktFinishKm = q
            }
        }
    }

    private suspend fun calculationKmChet() = withContext(Dispatchers.IO){
        var q = 1001
        var q1 = 1101
        var q2 = 1201
        var q3 = 1301
        var q4 = 1401
        var q5 = 1501
        var q6 = 1601
        var q7 = 1701
        var q8 = 1801
        var q9 = 1901

        var w = 1099
        var w1 = 1199
        var w2 = 1299
        var w3 = 1399
        var w4 = 1499
        var w5 = 1599
        var w6 = 1699
        var w7 = 1799
        var w8 = 1899
        var w9 = 1999

        var qkm = 2
        var wkm = 2

        var qpk = 1
        var wpk = 1

        while (q < 9999999) {
            q += 1000
            q1 += 1000
            q2 += 1000
            q3 += 1000
            q4 += 1000
            q5 += 1000
            q6 += 1000
            q7 += 1000
            q8 += 1000
            q9 += 1000

            w += 1000
            w1 += 1000
            w2 += 1000
            w3 += 1000
            w4 += 1000
            w5 += 1000
            w6 += 1000
            w7 += 1000
            w8 += 1000
            w9 += 1000

            qkm += 1
            wkm += 1

            if (distance1Recording >= q && distance1Recording <= w){
                qpk = 1
                kmDistance = qkm
                pkDistance = qpk

                faktStartKm = q
                faktFinishKm = w
            }

            if (distance1Recording >= q1 && distance1Recording <= w1){
                qpk = 2
                kmDistance = qkm
                pkDistance = qpk

                faktStartKm = q1
                faktFinishKm = w1
            }

            if (distance1Recording >= q2 && distance1Recording <= w2){
                qpk = 3
                kmDistance = qkm
                pkDistance = qpk

                faktStartKm = q2
                faktFinishKm = w2
            }

            if (distance1Recording >= q3 && distance1Recording <= w3){
                qpk = 4
                kmDistance = qkm
                pkDistance = qpk

                faktStartKm = q3
                faktFinishKm = w3
            }

            if (distance1Recording >= q4 && distance1Recording <= w4){
                qpk = 5
                kmDistance = qkm
                pkDistance = qpk

                faktStartKm = q4
                faktFinishKm = w4
            }

            if (distance1Recording >= q5 && distance1Recording <= w5){
                qpk = 6
                kmDistance = qkm
                pkDistance = qpk

                faktStartKm = q5
                faktFinishKm = w5
            }

            if (distance1Recording >= q6 && distance1Recording <= w6){
                qpk = 7
                kmDistance = qkm
                pkDistance = qpk

                faktStartKm = q6
                faktFinishKm = w6
            }

            if (distance1Recording >= q7 && distance1Recording <= w7){
                qpk = 8
                kmDistance = qkm
                pkDistance = qpk

                faktStartKm = q7
                faktFinishKm = w7
            }

            if (distance1Recording >= q8 && distance1Recording <= w8){
                qpk = 9
                kmDistance = qkm
                pkDistance = qpk

                faktStartKm = q8
                faktFinishKm = w8
            }

            if (distance1Recording >= q9 && distance1Recording <= w9){
                qpk = 10
                kmDistance = qkm
                pkDistance = qpk

                faktStartKm = q9
                faktFinishKm = w9
            }
        }
    }

    companion object{
        const val LOC_MODEL_INTENT_RECORDING = "loc_model_intent_recording"
        const val CHANNEL_ID_RECORDING_CHET = "channel_1_Recording_chet"
        const val CHANNEL_ID_RECORDING_NECHET = "channel_1_Recording_nechet"
        var isRunningRecording = false
    }
}
