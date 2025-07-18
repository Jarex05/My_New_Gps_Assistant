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

                                if (distance2Recording <= faktNachKmRedacktor + 999 && distance2Recording >= faktNachKmRedacktor + 951 && item.minusNechet != "" && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 949 && distance2Recording >= faktNachKmRedacktor + 901 && item.minusNechet != "" && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 899 && distance2Recording >= faktNachKmRedacktor + 851 && item.minusNechet != "" && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 849 && distance2Recording >= faktNachKmRedacktor + 801 && item.minusNechet != "" && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 799 && distance2Recording >= faktNachKmRedacktor + 751 && item.minusNechet != "" && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 749 && distance2Recording >= faktNachKmRedacktor + 701 && item.minusNechet != "" && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 699 && distance2Recording >= faktNachKmRedacktor + 651 && item.minusNechet != "" && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 649 && distance2Recording >= faktNachKmRedacktor + 601 && item.minusNechet != "" && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 599 && distance2Recording >= faktNachKmRedacktor + 551 && item.minusNechet != "" && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 549 && distance2Recording >= faktNachKmRedacktor + 501 && item.minusNechet != "" && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 499 && distance2Recording >= faktNachKmRedacktor + 451 && item.minusNechet != "" && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 449 && distance2Recording >= faktNachKmRedacktor + 401 && item.minusNechet != "" && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 399 && distance2Recording >= faktNachKmRedacktor + 351 && item.minusNechet != "" && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 349 && distance2Recording >= faktNachKmRedacktor + 301 && item.minusNechet != "" && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 299 && distance2Recording >= faktNachKmRedacktor + 251 && item.minusNechet != "" && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 249 && distance2Recording >= faktNachKmRedacktor + 201 && item.minusNechet != "" && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 199 && distance2Recording >= faktNachKmRedacktor + 151 && item.minusNechet != "" && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 149 && distance2Recording >= faktNachKmRedacktor + 101 && item.minusNechet != "" && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 99 && distance2Recording >= faktNachKmRedacktor + 51 && item.minusNechet != "" && isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 49 && distance2Recording >= faktNachKmRedacktor + 1 && item.minusNechet != "" && !isRedaktorMinusNechet){
                                    val minusNechet = item.minusNechet.toInt() / 20
                                    distance1Recording += minusNechet
                                    isRedaktorMinusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance1Recording <= faktNachKmRedacktor && distance1Recording >= faktNachKmRedacktor - 49 && item.minusNechet != "" && isRedaktorMinusNechet){
                                    distance2Recording = distance1Recording
                                    isRedaktorMinusNechet = !isRedaktorMinusNechet
                                }
                                if (distance1Recording <= faktNachKmRedacktor - 51 && distance1Recording >= faktNachKmRedacktor - 99 && item.minusNechet != "" && !isRedaktorMinusNechet){
                                    isRedaktorMinusNechet = true
                                }

                                // Конец отнимания метража

                                // Начало прибавления метража

                                if (distance2Recording <= faktNachKmRedacktor + 999 && distance2Recording >= faktNachKmRedacktor + 951 && item.plusNechet != "" && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 949 && distance2Recording >= faktNachKmRedacktor + 901 && item.plusNechet != "" && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 899 && distance2Recording >= faktNachKmRedacktor + 851 && item.plusNechet != "" && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 849 && distance2Recording >= faktNachKmRedacktor + 801 && item.plusNechet != "" && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 799 && distance2Recording >= faktNachKmRedacktor + 751 && item.plusNechet != "" && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 749 && distance2Recording >= faktNachKmRedacktor + 701 && item.plusNechet != "" && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 699 && distance2Recording >= faktNachKmRedacktor + 651 && item.plusNechet != "" && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 649 && distance2Recording >= faktNachKmRedacktor + 601 && item.plusNechet != "" && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 599 && distance2Recording >= faktNachKmRedacktor + 551 && item.plusNechet != "" && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 549 && distance2Recording >= faktNachKmRedacktor + 501 && item.plusNechet != "" && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 499 && distance2Recording >= faktNachKmRedacktor + 451 && item.plusNechet != "" && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 449 && distance2Recording >= faktNachKmRedacktor + 401 && item.plusNechet != "" && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 399 && distance2Recording >= faktNachKmRedacktor + 351 && item.plusNechet != "" && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 349 && distance2Recording >= faktNachKmRedacktor + 301 && item.plusNechet != "" && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 299 && distance2Recording >= faktNachKmRedacktor + 251 && item.plusNechet != "" && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 249 && distance2Recording >= faktNachKmRedacktor + 201 && item.plusNechet != "" && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 199 && distance2Recording >= faktNachKmRedacktor + 151 && item.plusNechet != "" && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 149 && distance2Recording >= faktNachKmRedacktor + 101 && item.plusNechet != "" && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor + 99 && distance2Recording >= faktNachKmRedacktor + 51 && item.plusNechet != "" && isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor + 49 && distance2Recording >= faktNachKmRedacktor + 1 && item.plusNechet != "" && !isRedaktorPlusNechet){
                                    val plusNechet = item.plusNechet.toInt() / 20
                                    distance1Recording -= plusNechet
                                    isRedaktorPlusNechet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording <= faktNachKmRedacktor && distance2Recording >= faktNachKmRedacktor - 49 && item.plusNechet != "" && isRedaktorPlusNechet){
                                    distance2Recording = distance1Recording
                                    isRedaktorPlusNechet = !isRedaktorPlusNechet
                                }
                                if (distance2Recording <= faktNachKmRedacktor - 51 && distance2Recording >= faktNachKmRedacktor - 99 && item.plusNechet != "" && !isRedaktorPlusNechet){
                                    isRedaktorPlusNechet = true
                                }

                                // Конец прибавления метража

                            }
                            myDbManagerRedacktor.closeDb()
                        }

                    } else {
                        distance1Recording += lastLocationRecording?.distanceTo(currentLocationRecording)!!

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

                                if (distance2Recording >= faktNachKmRedacktor - 999 && distance2Recording <= faktNachKmRedacktor - 951 && item.minusChet != "" && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 949 && distance2Recording <= faktNachKmRedacktor - 901 && item.minusChet != "" && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 899 && distance2Recording <= faktNachKmRedacktor - 851 && item.minusChet != "" && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 849 && distance2Recording <= faktNachKmRedacktor - 801 && item.minusChet != "" && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 799 && distance2Recording <= faktNachKmRedacktor - 751 && item.minusChet != "" && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 749 && distance2Recording <= faktNachKmRedacktor - 701 && item.minusChet != "" && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 699 && distance2Recording <= faktNachKmRedacktor - 651 && item.minusChet != "" && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 649 && distance2Recording <= faktNachKmRedacktor - 601 && item.minusChet != "" && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 599 && distance2Recording <= faktNachKmRedacktor - 551 && item.minusChet != "" && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 549 && distance2Recording <= faktNachKmRedacktor - 501 && item.minusChet != "" && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 499 && distance2Recording <= faktNachKmRedacktor - 451 && item.minusChet != "" && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 449 && distance2Recording <= faktNachKmRedacktor - 401 && item.minusChet != "" && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 399 && distance2Recording <= faktNachKmRedacktor - 351 && item.minusChet != "" && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 349 && distance2Recording <= faktNachKmRedacktor - 301 && item.minusChet != "" && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 299 && distance2Recording <= faktNachKmRedacktor - 251 && item.minusChet != "" && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 249 && distance2Recording <= faktNachKmRedacktor - 201 && item.minusChet != "" && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 199 && distance2Recording <= faktNachKmRedacktor - 151 && item.minusChet != "" && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 149 && distance2Recording <= faktNachKmRedacktor - 101 && item.minusChet != "" && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 99 && distance2Recording <= faktNachKmRedacktor - 51 && item.minusChet != "" && isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 49 && distance2Recording <= faktNachKmRedacktor - 1 && item.minusChet != "" && !isRedacktorMinusChet){
                                    val minusChet = item.minusChet.toInt() / 20
                                    distance1Recording -= minusChet
                                    isRedacktorMinusChet = true
                                }
                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance1Recording >= faktNachKmRedacktor && distance1Recording <= faktNachKmRedacktor + 49 && item.minusChet != "" && isRedacktorMinusChet){
                                    distance2Recording = distance1Recording
                                    isRedacktorMinusChet = !isRedacktorMinusChet
                                }
                                if (distance1Recording >= faktNachKmRedacktor + 51 && distance1Recording <= faktNachKmRedacktor + 99 && item.minusChet != "" && !isRedacktorMinusChet){
                                    isRedacktorMinusChet = true
                                }

                                // Конец отнимания метража

                                // Начало прибавления метража

                                if (distance2Recording >= faktNachKmRedacktor - 999 && distance2Recording <= faktNachKmRedacktor - 951 && item.plusChet != "" && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 949 && distance2Recording <= faktNachKmRedacktor - 901 && item.plusChet != "" && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 899 && distance2Recording <= faktNachKmRedacktor - 851 && item.plusChet != "" && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 849 && distance2Recording <= faktNachKmRedacktor - 801 && item.plusChet != "" && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 799 && distance2Recording <= faktNachKmRedacktor - 751 && item.plusChet != "" && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 749 && distance2Recording <= faktNachKmRedacktor - 701 && item.plusChet != "" && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 699 && distance2Recording <= faktNachKmRedacktor - 651 && item.plusChet != "" && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 649 && distance2Recording <= faktNachKmRedacktor - 601 && item.plusChet != "" && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 599 && distance2Recording <= faktNachKmRedacktor - 551 && item.plusChet != "" && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 549 && distance2Recording <= faktNachKmRedacktor - 501 && item.plusChet != "" && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 499 && distance2Recording <= faktNachKmRedacktor - 451 && item.plusChet != "" && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 449 && distance2Recording <= faktNachKmRedacktor - 401 && item.plusChet != "" && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 399 && distance2Recording <= faktNachKmRedacktor - 351 && item.plusChet != "" && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 349 && distance2Recording <= faktNachKmRedacktor - 301 && item.plusChet != "" && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 299 && distance2Recording <= faktNachKmRedacktor - 251 && item.plusChet != "" && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 249 && distance2Recording <= faktNachKmRedacktor - 201 && item.plusChet != "" && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 199 && distance2Recording <= faktNachKmRedacktor - 151 && item.plusChet != "" && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 149 && distance2Recording <= faktNachKmRedacktor - 101 && item.plusChet != "" && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor - 99 && distance2Recording <= faktNachKmRedacktor - 51 && item.plusChet != "" && isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor - 49 && distance2Recording <= faktNachKmRedacktor - 1 && item.plusChet != "" && !isRedacktorPlusChet){
                                    val plusChet = item.plusChet.toInt() / 20
                                    distance1Recording += plusChet
                                    isRedacktorPlusChet = true
                                }
                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance2Recording >= faktNachKmRedacktor && distance2Recording <= faktNachKmRedacktor + 49 && item.plusChet != "" && isRedacktorPlusChet){
                                    distance2Recording = distance1Recording
                                    isRedacktorPlusChet = !isRedacktorPlusChet
                                }
                                if (distance2Recording >= faktNachKmRedacktor + 51 && distance2Recording <= faktNachKmRedacktor + 99 && item.plusChet != "" && !isRedacktorPlusChet){
                                    isRedacktorPlusChet = true
                                }

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
