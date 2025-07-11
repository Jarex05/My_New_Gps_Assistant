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
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.location.Location
import android.media.AudioAttributes
import androidx.preference.PreferenceManager
import android.media.SoundPool
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.speech.tts.TextToSpeech
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
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.MyDbManagerBrake
import com.mikhail_ryumkin_r.my_gps_assistant.db.customField.MyDbManagerCustomField
import com.mikhail_ryumkin_r.my_gps_assistant.db.limitation.MyDbManagerLimitations
import com.mikhail_ryumkin_r.my_gps_assistant.db.pantograph.MyDbManagerPantograph
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.HomeFragment
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel.FragmentLatLongKmToServiceModel
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel.LocationModel
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.io.IOException
import java.util.Locale


@Suppress("DEPRECATION")
class MyLocationService : Service() {
    lateinit var textToSpeech: TextToSpeech
    private var isOgranichenie: Int = 0
    private var a: Int = 6909001
    private var b: Int = 6909099
    private var speedLimit = true
    private var isNeutralInsert600 = true

    //**********************************************

    val mySpeed = mutableListOf<Int>()

    //**********************************************

    val myOgranichenie = mutableListOf<Int>()
    val myPrev400 = mutableListOf<Int>()

    val pantograph800 = mutableListOf<Int>()
    val brake500 = mutableListOf<Int>()
    val neutralInsert600 = mutableListOf<Int>()

    val myLimitation = mutableListOf<Int>()
    val myPantograph = mutableListOf<Int>()
    val myBrake = mutableListOf<Int>()

    //**********************************************

    val myOgranichenieList = mutableListOf<Int>()
    val myPrev400List = mutableListOf<Int>()

    val pantograph800List = mutableListOf<Int>()
    val brake500List = mutableListOf<Int>()
    val neutralInsert600List = mutableListOf<Int>()

    val myLimitationList = mutableListOf<Int>()
    val myPantographList = mutableListOf<Int>()
    val myBrakeList = mutableListOf<Int>()

    //**********************************************

    private var isOgr15 = true
    private var isOgr25 = true
    private var isOgr40 = true
    private var isOgr50 = true
    private var isOgr55 = true
    private var isOgr60 = true
    private var isOgr65 = true
    private var isOgr70 = true
    private var isOgr75 = true

    private var ogranichenie1: Int = 0
    private var prev4001: Int = 0
    private var isPantograph8001: Int = 0
    private var isBrake5001: Int = 0
    private var isNeutralInsert6001: Int = 0
    private var limitation1: Int = 0
    private var brake1: Int = 0
    private var pantograph1: Int = 0

    private var uslChet = 0.0F
    private var sumCalculateUslChet = 1.0F

    private var isLimitationsChet400m15 = true
    private var isLimitationsChet400m25 = true
    private var isLimitationsChet400m40 = true
    private var isLimitationsChet400m50 = true
    private var isLimitationsChet400m55 = true
    private var isLimitationsChet400m60 = true
    private var isLimitationsChet400m65 = true
    private var isLimitationsChet400m70 = true
    private var isLimitationsChet400m75 = true

    private var int15: Int = 150
    private var int25: Int = 150
    private var int40: Int = 150
    private var int50: Int = 150
    private var int55: Int = 150
    private var int60: Int = 150
    private var int65: Int = 150
    private var int70: Int = 150
    private var int75: Int = 150

    var tvOgrChet15: String = ""
    var tvOgrChet25: String = ""
    var tvOgrChet40: String = ""
    var tvOgrChet50: String = ""
    var tvOgrChet55: String = ""
    var tvOgrChet60: String = ""
    var tvOgrChet65: String = ""
    var tvOgrChet70: String = ""
    var tvOgrChet75: String = ""

    var tvKmPkChet15: String = ""
    var tvKmPkChet25: String = ""
    var tvKmPkChet40: String = ""
    var tvKmPkChet50: String = ""
    var tvKmPkChet55: String = ""
    var tvKmPkChet60: String = ""
    var tvKmPkChet65: String = ""
    var tvKmPkChet70: String = ""
    var tvKmPkChet75: String = ""

    private var faktStartKmChet: Int = 0
    private var faktFinishKmChet: Int = 0

    private var titleStartChet: Int = 0
    private var piketStartChet: Int = 0
    private var faktNachKmChet: Int = 0
    private var piketNachKmChet: Int = 0

    private var titleFinishChet: Int = 0
    private var piketFinishChet: Int = 0
    private var speedChet: Int = 0
    private var speedChetMin: Int = 0
    private var faktEndKmChet: Int = 0
    private var piketEndKmChet: Int = 0

    private var titleStartPantographChet: Int = 0
    private var piketStartPantographChet: Int = 0
    private var faktNachKmPantographChet: Int = 0
    private var piketNachKmPantographChet: Int = 0

    private var titleStartBrakeChet: Int = 0
    private var piketStartBrakeChet: Int = 0
    private var faktNachKmBrakeChet: Int = 0
    private var piketNachKmBrakeChet: Int = 0

    private var isDebag = true
    private var distance: Float = 0.0f
    private var kmDistance: Int = 0
    private var pkDistance: Int = 0

    private var kmDistanceCalculation: Int = 0
    private var pkDistanceCalculation: Int = 0
    private var faktStartCalculation: Int = 0
    private var faktFinishCalculation: Int = 0

    private var fragmentLatLongToService: String = ""
    private var isTrack = true
    private val distanceKmList = ArrayList<ListLatLongKm>()
    private lateinit var geoPointsTrack: ArrayList<GeoPoint>

    private var lastLocation: Location? = null
    private lateinit var locProvider: FusedLocationProviderClient
    private lateinit var locRequest: LocationRequest

    private lateinit var soundPool: SoundPool
    private lateinit var assetManager: AssetManager

    private var voice15: Int = 0
    private var voice25: Int = 0
    private var voice40: Int = 0
    private var voice50: Int = 0
    private var voice55: Int = 0
    private var voice60: Int = 0
    private var voice65: Int = 0
    private var voice70: Int = 0
    private var voice75: Int = 0
    private var voiceprev: Int = 0
    private var pantograph: Int = 0
    private var brake: Int = 0
    private var songvipolneno: Int = 0

    private var ogr15: Int = 0
    private var ogr25: Int = 0
    private var ogr40: Int = 0
    private var ogr50: Int = 0
    private var ogr55: Int = 0
    private var ogr60: Int = 0
    private var ogr65: Int = 0
    private var ogr70: Int = 0
    private var ogr75: Int = 0

    private var streamID = 0

    private lateinit var myDbManagerLimitations: MyDbManagerLimitations
    private lateinit var myDbManagerPantograph: MyDbManagerPantograph
    private lateinit var myDbManagerBrake: MyDbManagerBrake
    private lateinit var myDbManagerCustomField: MyDbManagerCustomField

    private var titleStartCustomFieldChet = 0
    private var piketStartCustomFieldChet = 0
    private var faktNachKmCustomFieldChet = 0
    private var piketNachKmCustomFieldChet = 0
    private var customFieldChet = ""

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val receiverLatLongKmToService = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intentLatLongKmToService: Intent?) {
            if (intentLatLongKmToService?.action == HomeFragment.FRAGMENT_LAT_LONG_KM_TO_SERVICE){
                val fragmentLatLongKmToService = intentLatLongKmToService.getSerializableExtra(
                    HomeFragment.FRAGMENT_LAT_LONG_KM_TO_SERVICE) as FragmentLatLongKmToServiceModel
                if (isTrack && fragmentLatLongToService == "") {
                    fragmentLatLongToService = fragmentLatLongKmToService.fragmentLatLongKmToService
                    Log.d("MyLog", "FragmentLatLongKmToService: $fragmentLatLongToService")
                    isTrack = !isTrack
                }
            }
        }
    }

    private fun registerLatLongKmToService(){
        val locFilterLatLongKmToService = IntentFilter(HomeFragment.FRAGMENT_LAT_LONG_KM_TO_SERVICE)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiverLatLongKmToService, locFilterLatLongKmToService)
    }

    private suspend fun getLatLongKm() = withContext(Dispatchers.Main) {
        if (distanceKmList.indices.isEmpty()) {
            val list = fragmentLatLongToService.split("/")
            list.forEach {
                if (it.isEmpty()) return@forEach
                val points = it.split(",")
                distanceKmList.add(ListLatLongKm(points[0].toFloat(), points[1].toFloat(), points[2].toFloat()))
                geoPointsTrack.add(GeoPoint(points[0].toDouble(), points[1].toDouble()))
            }
        }
    }

    data class ListLatLongKm(val lat: Float,
                                 val long: Float,
                                 val distKm: Float){
        override fun toString(): String {
            return super.toString()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        myDbManagerPantograph = MyDbManagerPantograph(applicationContext)
        myDbManagerBrake = MyDbManagerBrake(applicationContext)
        myDbManagerLimitations = MyDbManagerLimitations(applicationContext)
        myDbManagerCustomField = MyDbManagerCustomField(applicationContext)
        startNotification()
        startLocationUpdates()
        registerLatLongKmToService()
        isRunning = true
        return START_STICKY
    }

    private fun playSound(sound: Int): Int {
        if (sound > 0) {
            streamID = soundPool.play(sound, 1F, 1F, 1, 0, 1F)
        }
        return streamID
    }

    private fun loadSound(fileName: String): Int {
        val afd: AssetFileDescriptor = try {
            application.assets.openFd(fileName)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("MyLog", "Не могу загрузить файл $fileName")

            return -1
        }
        return soundPool.load(afd,1)
    }

    override fun onCreate() {
        super.onCreate()
        geoPointsTrack = ArrayList()
        initLocation()

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .build()

        assetManager = assets
        voice15 = loadSound("voice15.mp3")
        voice25 = loadSound("voice25.mp3")
        voice40 = loadSound("voice40.mp3")
        voice50 = loadSound("voice50.mp3")
        voice55 = loadSound("voice55.mp3")
        voice60 = loadSound("voice60.mp3")
        voice65 = loadSound("voice65.mp3")
        voice70 = loadSound("voice70.mp3")
        voice75 = loadSound("voice75.mp3")
        voiceprev = loadSound("voiceprev.mp3")
        pantograph = loadSound("pantograph.mp3")
        brake = loadSound("brake.mp3")
        songvipolneno = loadSound("songvipolneno.mp3")

        ogr15 = loadSound("ogr15.mp3")
        ogr25 = loadSound("ogr25.mp3")
        ogr40 = loadSound("ogr40.mp3")
        ogr50 = loadSound("ogr50.mp3")
        ogr55 = loadSound("ogr55.mp3")
        ogr60 = loadSound("ogr60.mp3")
        ogr65 = loadSound("ogr65.mp3")
        ogr70 = loadSound("ogr70.mp3")
        ogr75 = loadSound("ogr75.mp3")
    }

    private val locCallback = object : LocationCallback() {
        override fun onLocationResult(lResult: LocationResult) {
            super.onLocationResult(lResult)
            val currentLocation = lResult.lastLocation
            if (lastLocation != null && currentLocation != null) {
                if (currentLocation.speed > 0.4 || isDebag){

                    CoroutineScope(Dispatchers.IO).launch {
                        calculationKm()
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        getLatLongKm()
                    }

                    val mLoc = Location("").apply {
                        latitude = currentLocation.latitude
                        longitude = currentLocation.longitude
                    }
                    var minDistance = 10000f
                    var distanceIndex = 0

                    for (i in distanceKmList.indices){
                        val pointLoc = Location("").apply {
                            latitude = distanceKmList[i].lat.toDouble()
                            longitude = distanceKmList[i].long.toDouble()
                        }
                        val dist = mLoc.distanceTo(pointLoc)
                        if (dist < 2000){
                            if (minDistance > dist){
                                minDistance = dist
                                distanceIndex = i
                            }
                        }
                    }

                    if (minDistance < 2000){
                        distance = distanceKmList[distanceIndex].distKm
                        Log.d("MyLog", "distance: $distance")
                    }

                    val v = SharedPref.Companion.getValue(baseContext)

                    if (v != 0) {

                    } else {

                        val myOgranichenie1 = myOgranichenie.toMutableSet().sorted()
                        val myPrev4001 = myPrev400.toMutableSet().sorted()
                        val pantograph8001 = pantograph800.toMutableSet().sorted()
                        val brake5001 = brake500.toMutableSet().sorted()
                        val neutralInsert6001 = neutralInsert600.toMutableSet().sorted()
                        val myLimitation1 = myLimitation.toMutableSet().sorted()
                        val myBrake1 = myBrake.toMutableSet().sorted()
                        val myPantograph1 = myPantograph.toMutableSet().sorted()

                        Log.d("MyLog", "mySpeed => $mySpeed")
                        Log.d("MyLog", "myOgranichenieList => $myOgranichenieList")
                        Log.d("MyLog", "myPrev400List => $myPrev400List")
                        Log.d("MyLog", "pantograph800List => $pantograph800List")
                        Log.d("MyLog", "brake500List => $brake500List")
                        Log.d("MyLog", "neutralInsert600List => $neutralInsert600List")
                        Log.d("MyLog", "myLimitationList => $myLimitationList")
                        Log.d("MyLog", "myBrakeList => $myBrakeList")
                        Log.d("MyLog", "myPantographList => $myPantographList")

                        if (myOgranichenie.isNotEmpty()){
                            for (itemOgranichenie in myOgranichenie1){
                                if (faktStartKmChet != 0) {
                                    if (faktStartKmChet == itemOgranichenie) {
                                        if (myOgranichenieList.isEmpty() && ogranichenie1 != itemOgranichenie) {
                                            myOgranichenieList.add(itemOgranichenie)
                                            ogranichenie1 = itemOgranichenie
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d("MyLog", "Список 'myOgranichenie1' пуст!")
                        }

                        if (myPrev400.isNotEmpty()){
                            for (itemMyPrev400 in myPrev4001){
                                if (faktStartKmChet != 0) {
                                    if (faktStartKmChet == itemMyPrev400) {
                                        if (myPrev400List.isEmpty() && prev4001 != itemMyPrev400) {
                                            myPrev400List.add(itemMyPrev400)
                                            prev4001 = itemMyPrev400
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d("MyLog", "Список 'myPrev4001' пуст!")
                        }

                        if (pantograph800.isNotEmpty()) {
                            for (itemPantograph800 in pantograph8001) {
                                if (faktStartKmChet != 0) {
                                    if (faktStartKmChet == itemPantograph800) {
                                        if (pantograph800List.isEmpty() && isPantograph8001 != itemPantograph800) {
                                            pantograph800List.add(itemPantograph800)
                                            isPantograph8001 = itemPantograph800
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d("MyLog", "Список 'pantograph8001' пуст!")
                        }

                        if (brake500.isNotEmpty()) {
                            for (itemBrake500 in brake5001) {
                                if (faktStartKmChet != 0) {
                                    if (faktStartKmChet == itemBrake500) {
                                        if (brake500List.isEmpty() && isBrake5001 != itemBrake500) {
                                            brake500List.add(itemBrake500)
                                            isBrake5001 = itemBrake500
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d("MyLog", "Список 'brake5001' пуст!")
                        }

                        if (neutralInsert600.isNotEmpty()) {
                            for (itemNeutralInsert600 in neutralInsert6001) {
                                if (faktStartKmChet != 0) {
                                    if (faktStartKmChet == itemNeutralInsert600) {
                                        if (neutralInsert600List.isEmpty() && isNeutralInsert6001 != itemNeutralInsert600) {
                                            neutralInsert600List.add(itemNeutralInsert600)
                                            isNeutralInsert6001 = itemNeutralInsert600
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d("MyLog", "Список 'neutralInsert6001' пуст!")
                        }

                        if (myLimitation.isNotEmpty()){
                            for (itemLimitation in myLimitation1){
                                if (faktStartKmChet != 0){
                                    if (faktStartKmChet + 2000 == itemLimitation){
                                        if (myLimitationList.isEmpty() && limitation1 != itemLimitation){
                                            myLimitationList.add(itemLimitation)
                                            limitation1 = itemLimitation
                                            Log.d("MyLog5", "km: $itemLimitation")
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d("MyLog", "Список 'myLimitation' пуст!")
                        }

                        if (myBrake.isNotEmpty()){
                            for (itemBrake in myBrake1){
                                if (faktStartKmChet != 0){
                                    if (faktStartKmChet + 4000 == itemBrake){
                                        if (myBrakeList.isEmpty() && brake1 != itemBrake){
                                            myBrakeList.add(itemBrake)
                                            brake1 = itemBrake
                                            Log.d("MyLog5", "Торможение: $itemBrake")
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d("MyLog", "Список 'myBrake' пуст!")
                        }

                        if (myPantograph.isNotEmpty()){
                            for (itemPantograph in myPantograph1){
                                if (faktStartKmChet != 0){
                                    if (faktStartKmChet + 3000 == itemPantograph){
                                        if (myPantographList.isEmpty() && pantograph1 != itemPantograph){
                                            myPantographList.add(itemPantograph)
                                            pantograph1 = itemPantograph
                                            Log.d("MyLog5", "Опускание: $itemPantograph")
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d("MyLog", "Список 'myPantograph' пуст!")
                        }

                        // Совпадение Нейтральных вставок, опускания и торможения ближе к цели с Остальными 5-ти звуками
                        // Совпадает 8 элементов
                        iteration1()

                        // Совпадение Нейтральных вставок, опускания и торможения ближе к цели с Остальными 4-мя звукам
                        // Совпадает 7 элементов Без превышения за 400 метров
                        iteration2()

                        // Совпадает 7 элементов Без Опускания токоприёмника за 3000 метров
                        iteration3()

                        // Совпадает 7 элементов Без Торможения за 4000 метров
                        iteration4()

                        // Совпадает 7 элементов Без Ограничения за 2000 метров
                        iteration5()

                        // Совпадает 6 элементов Без Торможения за 4000 метров и Опускания за 3000 метров
                        iteration6()

                        // Совпадает 6 элементов Без Ограничения за 2000 метров и Опускания за 3000 метров
                        iteration7()

                        // Совпадает 6 элементов Без Ограничения за 2000 метров и Торможения за 4000 метров
                        iteration8()

                        // Совпадает 5 элементов Без Ограничения за 2000 метров и Торможения за 4000 метров и Опускания за 3000 метров
                        iteration9()

                        // Совпадает 5 элементов Без Превышения за 400 метров и Торможения за 4000 метров и Опускания за 3000 метров
                        iteration10()

                        // Совпадает 5 элементов Без Превышения за 400 метров и Ограничения за 2000 метров и Опускания за 3000 метров
                        iteration11()

                        // Совпадает 5 элементов Без Превышения за 400 метров и Ограничения за 2000 метров и Торможения за 4000 метров
                        iteration12()

                        // Совпадает 4 элементов Без Превышения за 400 метров и Ограничения за 2000 метров и Торможения за 4000 метров и Опускания за 3000 метров
                        iteration13()

                        // Совпадение Нейтральных вставок, опускания и торможения ближе к цели с Остальными 4-мя звуками без Ограничения скорости в движении
                        // Совпадает 7 элементов
                        iteration14()

                        // Совпадает 6 элементов Без Опускания за 3000 метров
                        iteration15()

                        // Совпадает 6 элементов Без Торможения за 4000 метров
                        iteration16()

                        // Совпадает 6 элементов Без Ограничения за 2000 метров
                        iteration17()

                        // Совпадает 5 элементов Без Торможения за 4000 метров и Опускания за 3000 метров
                        iteration18()

                        // Совпадает 5 элементов Без Ограничения за 2000 метров и Опускания за 3000 метров
                        iteration19()

                        // Совпадает 5 элементов Без Ограничения за 2000 метров и Без Торможения за 4000 метров
                        iteration20()

                        // Совпадает 4 элемента Без Ограничения за 2000 метров и Без Торможения за 4000 метров и Без Опускания за 3000 метров
                        iteration21()

                        // Совпадает 6 элементов Без Ограничения в пути и превышения за 400 метров
                        iteration22()

                        // Совпадает 4 элемента Без Ограничения в пути и Превышения за 400 метров Торможения за 4000 метров и Опускания за 3000 метров
                        iteration23()

                        // Совпадает 5 элементов Без Ограничения в пути и Превышения за 400 метров и Опускания за 3000 метров
                        iteration24()

                        // Совпадает 5 элементов Без Ограничения в пути и Превышения за 400 метров и Торможения за 4000 метров
                        iteration25()

                        // Совпадает 5 элементов Без Ограничения в пути и Превышения за 400 метров и Ограничения за 2000 метров
                        iteration26()

                        // Совпадает 4 элемента Без Ограничения в пути и Превышения за 400 метров и Ограничения за 2000 метров и Опускания за 3000 метров
                        iteration27()

                        // Совпадает 4 элемента Без Ограничения в пути и Превышения за 400 метров и Ограничения за 2000 метров и Торможения за 4000 метров
                        iteration28()

                        // Совпадение ограничения в движении с остальными звуками
                        // Совпадают 5 элементов
                        iteration29()

                        // Совпадение 3-х элементов по порядку, без 5-ти выше стоящих звуков
                        iteration30()

                        //***********************************************************************

                        CoroutineScope(Dispatchers.Main).launch {
                            myPantograph.clear()
                            myDbManagerPantograph.openDb()
                            val dataListPantographs = myDbManagerPantograph.readDbDataPantographChet()
                            for (item in dataListPantographs){

                                titleStartPantographChet = item.startChet
                                piketStartPantographChet = item.picketStartChet

                                // Начало расчёта начала Опускания токоприемников по киллометро

                                var z = 1001
                                var kms = 2
                                val pkc = 1

                                while (z < 9999999){
                                    z += 1000
                                    kms += 1

                                    if (titleStartPantographChet == kms && piketStartPantographChet == pkc){
                                        faktNachKmPantographChet = z
                                        piketNachKmPantographChet = pkc
                                    }
                                }

                                var z1 = 1101
                                var kms1 = 2
                                val pkc1 = 2

                                while (z1 < 9999999){
                                    z1 += 1000
                                    kms1 += 1

                                    if (titleStartPantographChet == kms1 && piketStartPantographChet == pkc1){
                                        faktNachKmPantographChet = z1
                                        piketNachKmPantographChet = pkc1
                                    }
                                }

                                var z2 = 1201
                                var kms2 = 2
                                val pkc2 = 3

                                while (z2 < 9999999){
                                    z2 += 1000
                                    kms2 += 1

                                    if (titleStartPantographChet == kms2 && piketStartPantographChet == pkc2){
                                        faktNachKmPantographChet = z2
                                        piketNachKmPantographChet = pkc2
                                    }
                                }

                                var z3 = 1301
                                var kms3 = 2
                                val pkc3 = 4

                                while (z3 < 9999999){
                                    z3 += 1000
                                    kms3 += 1

                                    if (titleStartPantographChet == kms3 && piketStartPantographChet == pkc3){
                                        faktNachKmPantographChet = z3
                                        piketNachKmPantographChet = pkc3
                                    }
                                }

                                var z4 = 1401
                                var kms4 = 2
                                val pkc4 = 5

                                while (z4 < 9999999){
                                    z4 += 1000
                                    kms4 += 1

                                    if (titleStartPantographChet == kms4 && piketStartPantographChet == pkc4){
                                        faktNachKmPantographChet = z4
                                        piketNachKmPantographChet = pkc4
                                    }
                                }

                                var z5 = 1501
                                var kms5 = 2
                                val pkc5 = 6

                                while (z5 < 9999999){
                                    z5 += 1000
                                    kms5 += 1

                                    if (titleStartPantographChet == kms5 && piketStartPantographChet == pkc5){
                                        faktNachKmPantographChet = z5
                                        piketNachKmPantographChet = pkc5
                                    }
                                }

                                var z6 = 1601
                                var kms6 = 2
                                val pkc6 = 7

                                while (z6 < 9999999){
                                    z6 += 1000
                                    kms6 += 1

                                    if (titleStartPantographChet == kms6 && piketStartPantographChet == pkc6){
                                        faktNachKmPantographChet = z6
                                        piketNachKmPantographChet = pkc6
                                    }
                                }

                                var z7 = 1701
                                var kms7 = 2
                                val pkc7 = 8

                                while (z7 < 9999999){
                                    z7 += 1000
                                    kms7 += 1

                                    if (titleStartPantographChet == kms7 && piketStartPantographChet == pkc7){
                                        faktNachKmPantographChet = z7
                                        piketNachKmPantographChet = pkc7
                                    }
                                }

                                var z8 = 1801
                                var kms8 = 2
                                val pkc8 = 9

                                while (z8 < 9999999){
                                    z8 += 1000
                                    kms8 += 1

                                    if (titleStartPantographChet == kms8 && piketStartPantographChet == pkc8){
                                        faktNachKmPantographChet = z8
                                        piketNachKmPantographChet = pkc8
                                    }
                                }

                                var z9 = 1901
                                var kms9 = 2
                                val pkc9 = 10

                                while (z9 < 9999999){
                                    z9 += 1000
                                    kms9 += 1

                                    if (titleStartPantographChet == kms9 && piketStartPantographChet == pkc9){
                                        faktNachKmPantographChet = z9
                                        piketNachKmPantographChet = pkc9
                                    }
                                }

                                myPantograph.add(faktNachKmPantographChet)
                                pantograph800.add(faktNachKmPantographChet - 800)

                                // Конец расчёта начала Опускания токоприемников по киллометро
                            }
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            myBrake.clear()
                            myDbManagerBrake.openDb()
                            val dataListBrakes = myDbManagerBrake.readDbDataBrakeChet()
                            for (item in dataListBrakes){

                                titleStartBrakeChet = item.startChet
                                piketStartBrakeChet = item.picketStartChet

                                // Начало расчёта начала Торможения по киллометро

                                var z = 1001
                                var kms = 2
                                val pkc = 1

                                while (z < 9999999){
                                    z += 1000
                                    kms += 1

                                    if (titleStartBrakeChet == kms && piketStartBrakeChet == pkc){
                                        faktNachKmBrakeChet = z
                                        piketNachKmBrakeChet = pkc
                                    }
                                }

                                var z1 = 1101
                                var kms1 = 2
                                val pkc1 = 2

                                while (z1 < 9999999){
                                    z1 += 1000
                                    kms1 += 1

                                    if (titleStartBrakeChet == kms1 && piketStartBrakeChet == pkc1){
                                        faktNachKmBrakeChet = z1
                                        piketNachKmBrakeChet = pkc1
                                    }
                                }

                                var z2 = 1201
                                var kms2 = 2
                                val pkc2 = 3

                                while (z2 < 9999999){
                                    z2 += 1000
                                    kms2 += 1

                                    if (titleStartBrakeChet == kms2 && piketStartBrakeChet == pkc2){
                                        faktNachKmBrakeChet = z2
                                        piketNachKmBrakeChet = pkc2
                                    }
                                }

                                var z3 = 1301
                                var kms3 = 2
                                val pkc3 = 4

                                while (z3 < 9999999){
                                    z3 += 1000
                                    kms3 += 1

                                    if (titleStartBrakeChet == kms3 && piketStartBrakeChet == pkc3){
                                        faktNachKmBrakeChet = z3
                                        piketNachKmBrakeChet = pkc3
                                    }
                                }

                                var z4 = 1401
                                var kms4 = 2
                                val pkc4 = 5

                                while (z4 < 9999999){
                                    z4 += 1000
                                    kms4 += 1

                                    if (titleStartBrakeChet == kms4 && piketStartBrakeChet == pkc4){
                                        faktNachKmBrakeChet = z4
                                        piketNachKmBrakeChet = pkc4
                                    }
                                }

                                var z5 = 1501
                                var kms5 = 2
                                val pkc5 = 6

                                while (z5 < 9999999){
                                    z5 += 1000
                                    kms5 += 1

                                    if (titleStartBrakeChet == kms5 && piketStartBrakeChet == pkc5){
                                        faktNachKmBrakeChet = z5
                                        piketNachKmBrakeChet = pkc5
                                    }
                                }

                                var z6 = 1601
                                var kms6 = 2
                                val pkc6 = 7

                                while (z6 < 9999999){
                                    z6 += 1000
                                    kms6 += 1

                                    if (titleStartBrakeChet == kms6 && piketStartBrakeChet == pkc6){
                                        faktNachKmBrakeChet = z6
                                        piketNachKmBrakeChet = pkc6
                                    }
                                }

                                var z7 = 1701
                                var kms7 = 2
                                val pkc7 = 8

                                while (z7 < 9999999){
                                    z7 += 1000
                                    kms7 += 1

                                    if (titleStartBrakeChet == kms7 && piketStartBrakeChet == pkc7){
                                        faktNachKmBrakeChet = z7
                                        piketNachKmBrakeChet = pkc7
                                    }
                                }

                                var z8 = 1801
                                var kms8 = 2
                                val pkc8 = 9

                                while (z8 < 9999999){
                                    z8 += 1000
                                    kms8 += 1

                                    if (titleStartBrakeChet == kms8 && piketStartBrakeChet == pkc8){
                                        faktNachKmBrakeChet = z8
                                        piketNachKmBrakeChet = pkc8
                                    }
                                }

                                var z9 = 1901
                                var kms9 = 2
                                val pkc9 = 10

                                while (z9 < 9999999){
                                    z9 += 1000
                                    kms9 += 1

                                    if (titleStartBrakeChet == kms9 && piketStartBrakeChet == pkc9){
                                        faktNachKmBrakeChet = z9
                                        piketNachKmBrakeChet = pkc9
                                    }
                                }

                                myBrake.add(faktNachKmBrakeChet)
                                brake500.add(faktNachKmBrakeChet - 500)

                                // Конец расчёта начала Торможения по киллометро
                            }
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            myLimitation.clear()
                            myDbManagerLimitations.openDb()
                            val dataListLimitations = myDbManagerLimitations.readDbDataLimitationsChet()
                            sumCalculateUslChet = (uslChet * 14) + 50
                            for (item in dataListLimitations){

                                titleStartChet = item.startChet
                                piketStartChet = item.picketStartChet
                                titleFinishChet = item.finishChet
                                piketFinishChet = item.picketFinishChet
                                speedChet = item.speedChet

                                // Расчёт начала ограничения по киллометро

                                var z = 1001
                                var kms = 2
                                val pkc = 1

                                while (z < 9999999){
                                    z += 1000
                                    kms += 1

                                    if (titleStartChet == kms && piketStartChet == pkc){
                                        faktNachKmChet = z
                                        piketNachKmChet = pkc
                                    }
                                }

                                var z1 = 1101
                                var kms1 = 2
                                val pkc1 = 2

                                while (z1 < 9999999){
                                    z1 += 1000
                                    kms1 += 1

                                    if (titleStartChet == kms1 && piketStartChet == pkc1){
                                        faktNachKmChet = z1
                                        piketNachKmChet = pkc1
                                    }
                                }

                                var z2 = 1201
                                var kms2 = 2
                                val pkc2 = 3

                                while (z2 < 9999999){
                                    z2 += 1000
                                    kms2 += 1

                                    if (titleStartChet == kms2 && piketStartChet == pkc2){
                                        faktNachKmChet = z2
                                        piketNachKmChet = pkc2
                                    }
                                }

                                var z3 = 1301
                                var kms3 = 2
                                val pkc3 = 4

                                while (z3 < 9999999){
                                    z3 += 1000
                                    kms3 += 1

                                    if (titleStartChet == kms3 && piketStartChet == pkc3){
                                        faktNachKmChet = z3
                                        piketNachKmChet = pkc3
                                    }
                                }

                                var z4 = 1401
                                var kms4 = 2
                                val pkc4 = 5

                                while (z4 < 9999999){
                                    z4 += 1000
                                    kms4 += 1

                                    if (titleStartChet == kms4 && piketStartChet == pkc4){
                                        faktNachKmChet = z4
                                        piketNachKmChet = pkc4
                                    }
                                }

                                var z5 = 1501
                                var kms5 = 2
                                val pkc5 = 6

                                while (z5 < 9999999){
                                    z5 += 1000
                                    kms5 += 1

                                    if (titleStartChet == kms5 && piketStartChet == pkc5){
                                        faktNachKmChet = z5
                                        piketNachKmChet = pkc5
                                    }
                                }

                                var z6 = 1601
                                var kms6 = 2
                                val pkc6 = 7

                                while (z6 < 9999999){
                                    z6 += 1000
                                    kms6 += 1

                                    if (titleStartChet == kms6 && piketStartChet == pkc6){
                                        faktNachKmChet = z6
                                        piketNachKmChet = pkc6
                                    }
                                }

                                var z7 = 1701
                                var kms7 = 2
                                val pkc7 = 8

                                while (z7 < 9999999){
                                    z7 += 1000
                                    kms7 += 1

                                    if (titleStartChet == kms7 && piketStartChet == pkc7){
                                        faktNachKmChet = z7
                                        piketNachKmChet = pkc7
                                    }
                                }

                                var z8 = 1801
                                var kms8 = 2
                                val pkc8 = 9

                                while (z8 < 9999999){
                                    z8 += 1000
                                    kms8 += 1

                                    if (titleStartChet == kms8 && piketStartChet == pkc8){
                                        faktNachKmChet = z8
                                        piketNachKmChet = pkc8
                                    }
                                }

                                var z9 = 1901
                                var kms9 = 2
                                val pkc9 = 10

                                while (z9 < 9999999){
                                    z9 += 1000
                                    kms9 += 1

                                    if (titleStartChet == kms9 && piketStartChet == pkc9){
                                        faktNachKmChet = z9
                                        piketNachKmChet = pkc9
                                    }
                                }

                                // Конец расчёта начала ограничения по киллометро

                                // Расчёт конца ограничения по киллометро

                                var x = 1099
                                var kmx = 2
                                val pkx = 1

                                while (x < 9999999){
                                    x += 1000
                                    kmx += 1

                                    if (titleFinishChet == kmx && piketFinishChet == pkx){
                                        faktEndKmChet = x
                                        piketEndKmChet = pkx
                                    }
                                }

                                var x1 = 1199
                                var kmx1 = 2
                                val pkx1 = 2

                                while (x1 < 9999999){
                                    x1 += 1000
                                    kmx1 += 1

                                    if (titleFinishChet == kmx1 && piketFinishChet == pkx1){
                                        faktEndKmChet = x1
                                        piketEndKmChet = pkx1
                                    }
                                }

                                var x2 = 1299
                                var kmx2 = 2
                                val pkx2 = 3

                                while (x2 < 9999999){
                                    x2 += 1000
                                    kmx2 += 1

                                    if (titleFinishChet == kmx2 && piketFinishChet == pkx2){
                                        faktEndKmChet = x2
                                        piketEndKmChet = pkx2
                                    }
                                }

                                var x3 = 1399
                                var kmx3 = 2
                                val pkx3 = 4

                                while (x3 < 9999999){
                                    x3 += 1000
                                    kmx3 += 1

                                    if (titleFinishChet == kmx3 && piketFinishChet == pkx3){
                                        faktEndKmChet = x3
                                        piketEndKmChet = pkx3
                                    }
                                }

                                var x4 = 1499
                                var kmx4 = 2
                                val pkx4 = 5

                                while (x4 < 9999999){
                                    x4 += 1000
                                    kmx4 += 1

                                    if (titleFinishChet == kmx4 && piketFinishChet == pkx4){
                                        faktEndKmChet = x4
                                        piketEndKmChet = pkx4
                                    }
                                }

                                var x5 = 1599
                                var kmx5 = 2
                                val pkx5 = 6

                                while (x5 < 9999999){
                                    x5 += 1000
                                    kmx5 += 1

                                    if (titleFinishChet == kmx5 && piketFinishChet == pkx5){
                                        faktEndKmChet = x5
                                        piketEndKmChet = pkx5
                                    }
                                }

                                var x6 = 1699
                                var kmx6 = 2
                                val pkx6 = 7

                                while (x6 < 9999999){
                                    x6 += 1000
                                    kmx6 += 1

                                    if (titleFinishChet == kmx6 && piketFinishChet == pkx6){
                                        faktEndKmChet = x6
                                        piketEndKmChet = pkx6
                                    }
                                }

                                var x7 = 1799
                                var kmx7 = 2
                                val pkx7 = 8

                                while (x7 < 9999999){
                                    x7 += 1000
                                    kmx7 += 1

                                    if (titleFinishChet == kmx7 && piketFinishChet == pkx7){
                                        faktEndKmChet = x7
                                        piketEndKmChet = pkx7
                                    }
                                }

                                var x8 = 1899
                                var kmx8 = 2
                                val pkx8 = 9

                                while (x8 < 9999999){
                                    x8 += 1000
                                    kmx8 += 1

                                    if (titleFinishChet == kmx8 && piketFinishChet == pkx8){
                                        faktEndKmChet = x8
                                        piketEndKmChet = pkx8
                                    }
                                }

                                var x9 = 1999
                                var kmx9 = 2
                                val pkx9 = 10

                                while (x9 < 9999999){
                                    x9 += 1000
                                    kmx9 += 1

                                    if (titleFinishChet == kmx9 && piketFinishChet == pkx9){
                                        faktEndKmChet = x9
                                        piketEndKmChet = pkx9
                                    }
                                }

                                myLimitation.add(faktNachKmChet)

                                // Конец расчёта конца ограничения по киллометро

                                // Начало оповещения ограничения скорости за 2 км/ч

                                if (distance >= faktNachKmChet - 2050 && distance <= faktNachKmChet - 2001 && speedLimit){
                                    Log.d("MyLog3", "Зашли в скорость!")
                                    speedLimit = !speedLimit

                                    val arrayListSpeed = mutableListOf(int15, int25, int40, int50, int55, int60, int65, int70, int75)

                                    if (speedChet == 15){
                                        int15 = item.speedChet
                                        arrayListSpeed.add(int15)
                                    }
                                    if (speedChet == 25){
                                        int25 = item.speedChet
                                        arrayListSpeed.add(int25)
                                    }
                                    if (speedChet == 40){
                                        int40 = item.speedChet
                                        arrayListSpeed.add(int40)
                                    }
                                    if (speedChet == 50){
                                        int50 = item.speedChet
                                        arrayListSpeed.add(int50)
                                    }
                                    if (speedChet == 55){
                                        int55 = item.speedChet
                                        arrayListSpeed.add(int55)
                                    }
                                    if (speedChet == 60){
                                        int60 = item.speedChet
                                        arrayListSpeed.add(int60)
                                    }
                                    if (speedChet == 65){
                                        int65 = item.speedChet
                                        arrayListSpeed.add(int65)
                                    }
                                    if (speedChet == 70){
                                        int70 = item.speedChet
                                        arrayListSpeed.add(int70)
                                    }
                                    if (speedChet == 75){
                                        int75 = item.speedChet
                                        arrayListSpeed.add(int75)
                                    }

                                    Log.d("MyLog1", "minSpeed = ${arrayListSpeed.min()}")
                                    speedChetMin = arrayListSpeed.min()
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet - 1999 && distance <= faktNachKmChet - 1951 && item.speedChet == 15 && !speedLimit){
                                    Log.d("MyLog3", "Зашли в 15")
                                    mySpeed.add(15)
                                    int15 = 150
                                    speedLimit = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet - 1999 && distance <= faktNachKmChet - 1951 && item.speedChet == 25 && !speedLimit){
                                    Log.d("MyLog3", "Зашли в 25!")
                                    mySpeed.add(25)
                                    int25 = 150
                                    speedLimit = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet - 1999 && distance <= faktNachKmChet - 1951 && item.speedChet == 40 && !speedLimit){
                                    Log.d("MyLog3", "Зашли в 40!")
                                    mySpeed.add(40)
                                    int40 = 150
                                    speedLimit = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet - 1999 && distance <= faktNachKmChet - 1951 && item.speedChet == 50 && !speedLimit){
                                    Log.d("MyLog3", "Зашли в 50!")
                                    mySpeed.add(50)
                                    int50 = 150
                                    speedLimit = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet - 1999 && distance <= faktNachKmChet - 1951 && item.speedChet == 55 && !speedLimit){
                                    Log.d("MyLog3", "Зашли в 55!")
                                    mySpeed.add(55)
                                    int55 = 150
                                    speedLimit = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet - 1999 && distance <= faktNachKmChet - 1951 && item.speedChet == 60 && !speedLimit){
                                    Log.d("MyLog3", "Зашли в 60!")
                                    mySpeed.add(60)
                                    int60 = 150
                                    speedLimit = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet - 1999 && distance <= faktNachKmChet - 1951 && item.speedChet == 65 && !speedLimit){
                                    Log.d("MyLog3", "Зашли в 65!")
                                    mySpeed.add(65)
                                    int65 = 150
                                    speedLimit = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet - 1999 && distance <= faktNachKmChet - 1951 && item.speedChet == 70 && !speedLimit){
                                    Log.d("MyLog3", "Зашли в 70!")
                                    mySpeed.add(70)
                                    int70 = 150
                                    speedLimit = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet - 1999 && distance <= faktNachKmChet - 1951 && item.speedChet == 75 && !speedLimit){
                                    Log.d("MyLog3", "Зашли в 75!")
                                    mySpeed.add(75)
                                    int75 = 150
                                    speedLimit = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                // Конец оповещения ограничения скорости за 2 км/ч

                                // Предупреждение о превышении за 400 метров до предупреждения

                                if (currentLocation.speed * 3.6 >= 15 && distance >= faktNachKmChet - 549 && distance <= faktNachKmChet - 501 && item.speedChet == 15 && isLimitationsChet400m15){
                                    myPrev400.clear()
                                    val myPrev = faktStartKmChet + 100
                                    myPrev400.add(myPrev)
                                    isLimitationsChet400m15 = !isLimitationsChet400m15
                                }
                                if (distance >= faktNachKmChet - 499 && distance <= faktNachKmChet - 451 && item.speedChet == 15){
                                    isLimitationsChet400m15 = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (currentLocation.speed * 3.6 >= 25 && distance >= faktNachKmChet - 549 && distance <= faktNachKmChet - 501 && item.speedChet == 25 && isLimitationsChet400m25){
                                    myPrev400.clear()
                                    val myPrev = faktStartKmChet + 100
                                    myPrev400.add(myPrev)
                                    isLimitationsChet400m25 = !isLimitationsChet400m25
                                }
                                if (distance >= faktNachKmChet - 499 && distance <= faktNachKmChet - 451 && item.speedChet == 25){
                                    isLimitationsChet400m25 = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (currentLocation.speed * 3.6 >= 40 && distance >= faktNachKmChet - 549 && distance <= faktNachKmChet - 501 && item.speedChet == 40 && isLimitationsChet400m40){
                                    myPrev400.clear()
                                    val myPrev = faktStartKmChet + 100
                                    myPrev400.add(myPrev)
                                    isLimitationsChet400m40 = !isLimitationsChet400m40
                                }
                                if (distance >= faktNachKmChet - 499 && distance <= faktNachKmChet - 451 && item.speedChet == 40){
                                    isLimitationsChet400m40 = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (currentLocation.speed * 3.6 >= 50 && distance >= faktNachKmChet - 549 && distance <= faktNachKmChet - 501 && item.speedChet == 50 && isLimitationsChet400m50){
                                    myPrev400.clear()
                                    val myPrev = faktStartKmChet + 100
                                    myPrev400.add(myPrev)
                                    isLimitationsChet400m50 = !isLimitationsChet400m50
                                }
                                if (distance >= faktNachKmChet - 499 && distance <= faktNachKmChet - 451 && item.speedChet == 50){
                                    isLimitationsChet400m50 = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (currentLocation.speed * 3.6 >= 55 && distance >= faktNachKmChet - 549 && distance <= faktNachKmChet - 501 && item.speedChet == 55 && isLimitationsChet400m55){
                                    myPrev400.clear()
                                    val myPrev = faktStartKmChet + 100
                                    myPrev400.add(myPrev)
                                    isLimitationsChet400m55 = !isLimitationsChet400m55
                                }
                                if (distance >= faktNachKmChet - 499 && distance <= faktNachKmChet - 451 && item.speedChet == 55){
                                    isLimitationsChet400m55 = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (currentLocation.speed * 3.6 >= 60 && distance >= faktNachKmChet - 549 && distance <= faktNachKmChet - 501 && item.speedChet == 60 && isLimitationsChet400m60){
                                    myPrev400.clear()
                                    val myPrev = faktStartKmChet + 100
                                    myPrev400.add(myPrev)
                                    isLimitationsChet400m60 = !isLimitationsChet400m60
                                }
                                if (distance >= faktNachKmChet - 499 && distance <= faktNachKmChet - 451 && item.speedChet == 60){
                                    isLimitationsChet400m60 = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (currentLocation.speed * 3.6 >= 65 && distance >= faktNachKmChet - 549 && distance <= faktNachKmChet - 501 && item.speedChet == 65 && isLimitationsChet400m65){
                                    myPrev400.clear()
                                    val myPrev = faktStartKmChet + 100
                                    myPrev400.add(myPrev)
                                    isLimitationsChet400m65 = !isLimitationsChet400m65
                                }
                                if (distance >= faktNachKmChet - 499 && distance <= faktNachKmChet - 451 && item.speedChet == 65){
                                    isLimitationsChet400m65 = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (currentLocation.speed * 3.6 >= 70 && distance >= faktNachKmChet - 549 && distance <= faktNachKmChet - 501 && item.speedChet == 70 && isLimitationsChet400m70){
                                    myPrev400.clear()
                                    val myPrev = faktStartKmChet + 100
                                    myPrev400.add(myPrev)
                                    isLimitationsChet400m70 = !isLimitationsChet400m70
                                }
                                if (distance >= faktNachKmChet - 499 && distance <= faktNachKmChet - 451 && item.speedChet == 70){
                                    isLimitationsChet400m70 = true
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (currentLocation.speed * 3.6 >= 75 && distance >= faktNachKmChet - 549 && distance <= faktNachKmChet - 501 && item.speedChet == 75 && isLimitationsChet400m75){
                                    myPrev400.clear()
                                    val myPrev = faktStartKmChet + 100
                                    myPrev400.add(myPrev)
                                    isLimitationsChet400m75 = !isLimitationsChet400m75
                                }
                                if (distance >= faktNachKmChet - 499 && distance <= faktNachKmChet - 451 && item.speedChet == 75){
                                    isLimitationsChet400m75 = true
                                }

                                // Конец предупреждение о превышении за 400 метров до предупреждения

                                // Начало оповещения о нейтральных вставках

                                if (distance >= a - 100 && distance <= b - 100 && isNeutralInsert600) {
                                    Log.d("MyLog1", "Нейтральная вставка")
                                    neutralInsert600.clear()
                                    val myNeutralInsert = faktStartKmChet + 100
                                    Log.d("MyLog1", "myNeutralInsert = $myNeutralInsert")
                                    neutralInsert600.add(myNeutralInsert)
                                    isNeutralInsert600 = !isNeutralInsert600
                                }

                                //*************************************************************************************

                                if (distance >= 6927301 && distance <= 6927399 && isNeutralInsert600) {
                                    neutralInsert600.clear()
                                    val myNeutralInsert = faktStartKmChet + 100
                                    neutralInsert600.add(myNeutralInsert)
                                    isNeutralInsert600 = !isNeutralInsert600
                                }
                                if (distance >= 6927401 && distance <= 6927499) {
                                    isNeutralInsert600 == true
                                }

                                //*************************************************************************************

                                if (distance >= 6956201 && distance <= 6956299 && isNeutralInsert600) {
                                    neutralInsert600.clear()
                                    val myNeutralInsert = faktStartKmChet + 100
                                    neutralInsert600.add(myNeutralInsert)
                                    isNeutralInsert600 = !isNeutralInsert600
                                }
                                if (distance >= 6956301 && distance <= 6956399) {
                                    isNeutralInsert600 == true
                                }

                                //*************************************************************************************

                                if (distance >= 6999801 && distance <= 6999899 && isNeutralInsert600) {
                                    neutralInsert600.clear()
                                    val myNeutralInsert = faktStartKmChet + 100
                                    neutralInsert600.add(myNeutralInsert)
                                    isNeutralInsert600 = !isNeutralInsert600
                                }
                                if (distance >= 6999901 && distance <= 6999999) {
                                    isNeutralInsert600 == true
                                }

                                //*************************************************************************************

                                if (distance >= 7036001 && distance <= 7036099 && isNeutralInsert600) {
                                    neutralInsert600.clear()
                                    val myNeutralInsert = faktStartKmChet + 100
                                    neutralInsert600.add(myNeutralInsert)
                                    isNeutralInsert600 = !isNeutralInsert600
                                }
                                if (distance >= 7036101 && distance <= 7036199) {
                                    isNeutralInsert600 == true
                                }

                                //*************************************************************************************

                                if (distance >= 7075301 && distance <= 7075399 && isNeutralInsert600) {
                                    neutralInsert600.clear()
                                    val myNeutralInsert = faktStartKmChet + 100
                                    neutralInsert600.add(myNeutralInsert)
                                    isNeutralInsert600 = !isNeutralInsert600
                                }
                                if (distance >= 7075401 && distance <= 7075499) {
                                    isNeutralInsert600 == true
                                }

                                //*************************************************************************************

                                if (distance >= 7117301 && distance <= 7117399 && isNeutralInsert600) {
                                    neutralInsert600.clear()
                                    val myNeutralInsert = faktStartKmChet + 100
                                    neutralInsert600.add(myNeutralInsert)
                                    isNeutralInsert600 = !isNeutralInsert600
                                }
                                if (distance >= 7117401 && distance <= 7117499) {
                                    isNeutralInsert600 == true
                                }

                                //*************************************************************************************

                                if (distance >= 7154001 && distance <= 7154099 && isNeutralInsert600) {
                                    neutralInsert600.clear()
                                    val myNeutralInsert = faktStartKmChet + 100
                                    neutralInsert600.add(myNeutralInsert)
                                    isNeutralInsert600 = !isNeutralInsert600
                                }
                                if (distance >= 7154101 && distance <= 7154199) {
                                    isNeutralInsert600 == true
                                }

                                //*************************************************************************************

                                // Конец оповещения о нейтральных вставках

                                // Предупреждение о превышении ограничения скорости

                                if (distance >= faktNachKmChet && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 15){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (currentLocation.speed * 3.6 >= 14 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 15 && isOgr15){
                                        if (isOgranichenie == 0) {
                                            myOgranichenie.clear()
                                            isOgranichenie = 15
                                            val myOgr = faktStartKmChet + 200
                                            Log.d("MyLog1", "myOgr = $myOgr")
                                            myOgranichenie.add(myOgr)
                                        }
                                        isOgr15 = false
                                    } else if (currentLocation.speed * 3.6 <= 12 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 15) {
                                        isOgr15 = true
                                    }
                                }
                                if (distance >= faktNachKmChet - 2000 && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 15){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (distance >= faktNachKmChet - 2000 && distance <= faktNachKmChet + sumDistance && item.speedChet == 15){
                                        val nachKm15 = faktNachKmChet
                                        val endKm15 = faktEndKmChet
                                        val speed15 = item.speedChet
                                        if (distance >= nachKm15 - 2000 && distance <= endKm15 + sumCalculateUslChet && speed15 == 15){
                                            tvOgrChet15 = "$speedChet"
                                            tvKmPkChet15 = "$titleStartChet км $piketStartChet пк - $titleFinishChet км $piketFinishChet пк"
                                        }
                                    }
                                }
                                if (distance >= faktEndKmChet + sumCalculateUslChet && distance <= faktEndKmChet + sumCalculateUslChet + 50 && item.speedChet == 15){
                                    tvOgrChet15 = ""
                                    tvKmPkChet15 = ""
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 25){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (currentLocation.speed * 3.6 >= 24 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 25 && isOgr25){
                                        if (isOgranichenie == 0) {
                                            myOgranichenie.clear()
                                            isOgranichenie = 25
                                            val myOgr = faktStartKmChet + 200
                                            myOgranichenie.add(myOgr)
                                        }

                                        isOgr25 = false
                                    } else if (currentLocation.speed * 3.6 <= 22 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 25) {
                                        isOgr25 = true
                                    }
                                }
                                if (distance >= faktNachKmChet - 2000 && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 25){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (distance >= faktNachKmChet - 2000 && distance <= faktNachKmChet + sumDistance && item.speedChet == 25){
                                        val nachKm25 = faktNachKmChet
                                        val endKm25 = faktEndKmChet
                                        val speed25 = item.speedChet
                                        if (distance >= nachKm25 - 2000 && distance <= endKm25 + sumCalculateUslChet && speed25 == 25){
                                            tvOgrChet25 = "$speedChet"
                                            tvKmPkChet25 = "$titleStartChet км $piketStartChet пк - $titleFinishChet км $piketFinishChet пк"
                                        }
                                    }
                                }
                                if (distance >= faktEndKmChet + sumCalculateUslChet && distance <= faktEndKmChet + sumCalculateUslChet + 50 && item.speedChet == 25){
                                    tvOgrChet25 = ""
                                    tvKmPkChet25 = ""
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 40){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (currentLocation.speed * 3.6 >= 39 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 40 && isOgr40){
                                        if (isOgranichenie == 0) {
                                            myOgranichenie.clear()
                                            isOgranichenie = 40
                                            val myOgr = faktStartKmChet + 200
                                            myOgranichenie.add(myOgr)
                                        }

                                        isOgr40 = false
                                    } else if (currentLocation.speed * 3.6 <= 37 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 40) {
                                        isOgr40 = true
                                    }
                                }
                                if (distance >= faktNachKmChet - 2000 && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 40){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (distance >= faktNachKmChet - 2000 && distance <= faktNachKmChet + sumDistance && item.speedChet == 40){
                                        val nachKm40 = faktNachKmChet
                                        val endKm40 = faktEndKmChet
                                        val speed40 = item.speedChet
                                        if (distance >= nachKm40 - 2000 && distance <= endKm40 + sumCalculateUslChet && speed40 == 40){
                                            tvOgrChet40 = "$speedChet"
                                            tvKmPkChet40 = "$titleStartChet км $piketStartChet пк - $titleFinishChet км $piketFinishChet пк"
                                        }
                                    }
                                }
                                if (distance >= faktEndKmChet + sumCalculateUslChet && distance <= faktEndKmChet + sumCalculateUslChet + 50 && item.speedChet == 40){
                                    tvOgrChet40 = ""
                                    tvKmPkChet40 = ""
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 50){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (currentLocation.speed * 3.6 >= 49 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 50 && isOgr50){
                                        if (isOgranichenie == 0) {
                                            myOgranichenie.clear()
                                            isOgranichenie = 50
                                            val myOgr = faktStartKmChet + 200
                                            myOgranichenie.add(myOgr)
                                        }

                                        isOgr50 = false
                                    } else if (currentLocation.speed * 3.6 <= 47 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 50) {
                                        isOgr50 = true
                                    }
                                }
                                if (distance >= faktNachKmChet - 2000 && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 50){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (distance >= faktNachKmChet - 2000 && distance <= faktNachKmChet + sumDistance && item.speedChet == 50){
                                        val nachKm50 = faktNachKmChet
                                        val endKm50 = faktEndKmChet
                                        val speed50 = item.speedChet
                                        if (distance >= nachKm50 - 2000 && distance <= endKm50 + sumCalculateUslChet && speed50 == 50){
                                            tvOgrChet50 = "$speedChet"
                                            tvKmPkChet50 = "$titleStartChet км $piketStartChet пк - $titleFinishChet км $piketFinishChet пк"
                                        }
                                    }
                                }
                                if (distance >= faktEndKmChet + sumCalculateUslChet && distance <= faktEndKmChet + sumCalculateUslChet + 50 && item.speedChet == 50){
                                    tvOgrChet50 = ""
                                    tvKmPkChet50 = ""
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 55){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (currentLocation.speed * 3.6 >= 54 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 55 && isOgr55){
                                        if (isOgranichenie == 0) {
                                            myOgranichenie.clear()
                                            isOgranichenie = 55
                                            val myOgr = faktStartKmChet + 200
                                            myOgranichenie.add(myOgr)
                                        }

                                        isOgr55 = false
                                    } else if (currentLocation.speed * 3.6 <= 52 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 55) {
                                        isOgr55 = true
                                    }
                                }
                                if (distance >= faktNachKmChet - 2000 && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 55){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (distance >= faktNachKmChet - 2000 && distance <= faktNachKmChet + sumDistance && item.speedChet == 55){
                                        val nachKm55 = faktNachKmChet
                                        val endKm55 = faktEndKmChet
                                        val speed55 = item.speedChet
                                        if (distance >= nachKm55 - 2000 && distance <= endKm55 + sumCalculateUslChet && speed55 == 55){
                                            tvOgrChet55 = "$speedChet"
                                            tvKmPkChet55 = "$titleStartChet км $piketStartChet пк - $titleFinishChet км $piketFinishChet пк"
                                        }
                                    }
                                }
                                if (distance >= faktEndKmChet + sumCalculateUslChet && distance <= faktEndKmChet + sumCalculateUslChet + 50 && item.speedChet == 55){
                                    tvOgrChet55 = ""
                                    tvKmPkChet55 = ""
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 60){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (currentLocation.speed * 3.6 >= 59 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 60 && isOgr60){
                                        if (isOgranichenie == 0) {
                                            myOgranichenie.clear()
                                            isOgranichenie = 60
                                            val myOgr = faktStartKmChet + 200
                                            myOgranichenie.add(myOgr)
                                        }

                                        isOgr60 = false
                                    } else if (currentLocation.speed * 3.6 <= 57 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 60) {
                                        isOgr60 = true
                                    }
                                }
                                if (distance >= faktNachKmChet - 2000 && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 60){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (distance >= faktNachKmChet - 2000 && distance <= faktNachKmChet + sumDistance && item.speedChet == 60){
                                        val nachKm60 = faktNachKmChet
                                        val endKm60 = faktEndKmChet
                                        val speed60 = item.speedChet
                                        if (distance >= nachKm60 - 2000 && distance <= endKm60 + sumCalculateUslChet && speed60 == 60){
                                            tvOgrChet60 = "$speedChet"
                                            tvKmPkChet60 = "$titleStartChet км $piketStartChet пк - $titleFinishChet км $piketFinishChet пк"
                                        }
                                    }
                                }
                                if (distance >= faktEndKmChet + sumCalculateUslChet && distance <= faktEndKmChet + sumCalculateUslChet + 50 && item.speedChet == 60){
                                    tvOgrChet60 = ""
                                    tvKmPkChet60 = ""
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 65){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (currentLocation.speed * 3.6 >= 64 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 65 && isOgr65){
                                        if (isOgranichenie == 0) {
                                            myOgranichenie.clear()
                                            isOgranichenie = 65
                                            val myOgr = faktStartKmChet + 200
                                            myOgranichenie.add(myOgr)
                                        }

                                        isOgr65 = false
                                    } else if (currentLocation.speed * 3.6 <= 62 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 65) {
                                        isOgr65 = true
                                    }
                                }
                                if (distance >= faktNachKmChet - 2000 && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 65){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (distance >= faktNachKmChet - 2000 && distance <= faktNachKmChet + sumDistance && item.speedChet == 65){
                                        val nachKm65 = faktNachKmChet
                                        val endKm65 = faktEndKmChet
                                        val speed65 = item.speedChet
                                        if (distance >= nachKm65 - 2000 && distance <= endKm65 + sumCalculateUslChet && speed65 == 65){
                                            tvOgrChet65 = "$speedChet"
                                            tvKmPkChet65 = "$titleStartChet км $piketStartChet пк - $titleFinishChet км $piketFinishChet пк"
                                        }
                                    }
                                }
                                if (distance >= faktEndKmChet + sumCalculateUslChet && distance <= faktEndKmChet + sumCalculateUslChet + 50 && item.speedChet == 65){
                                    tvOgrChet65 = ""
                                    tvKmPkChet65 = ""
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 70){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (currentLocation.speed * 3.6 >= 69 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 70 && isOgr70){
                                        if (isOgranichenie == 0) {
                                            myOgranichenie.clear()
                                            isOgranichenie = 70
                                            val myOgr = faktStartKmChet + 200
                                            myOgranichenie.add(myOgr)
                                        }

                                        isOgr70 = false
                                    } else if (currentLocation.speed * 3.6 <= 67 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 70) {
                                        isOgr70 = true
                                    }
                                }
                                if (distance >= faktNachKmChet - 2000 && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 70){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (distance >= faktNachKmChet - 2000 && distance <= faktNachKmChet + sumDistance && item.speedChet == 70){
                                        val nachKm70 = faktNachKmChet
                                        val endKm70 = faktEndKmChet
                                        val speed70 = item.speedChet
                                        if (distance >= nachKm70 - 2000 && distance <= endKm70 + sumCalculateUslChet && speed70 == 70){
                                            tvOgrChet70 = "$speedChet"
                                            tvKmPkChet70 = "$titleStartChet км $piketStartChet пк - $titleFinishChet км $piketFinishChet пк"
                                        }
                                    }
                                }
                                if (distance >= faktEndKmChet + sumCalculateUslChet && distance <= faktEndKmChet + sumCalculateUslChet + 50 && item.speedChet == 70){
                                    tvOgrChet70 = ""
                                    tvKmPkChet70 = ""
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                if (distance >= faktNachKmChet && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 75){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (currentLocation.speed * 3.6 >= 74 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 75 && isOgr75){
                                        if (isOgranichenie == 0) {
                                            myOgranichenie.clear()
                                            isOgranichenie = 75
                                            val myOgr = faktStartKmChet + 200
                                            myOgranichenie.add(myOgr)
                                        }

                                        isOgr75 = false
                                    } else if (currentLocation.speed * 3.6 <= 72 && distance >= faktNachKmChet && distance <= faktNachKmChet + sumDistance && item.speedChet == 75) {
                                        isOgr75 = true
                                    }
                                }
                                if (distance >= faktNachKmChet - 2000 && distance <= faktEndKmChet + sumCalculateUslChet && item.speedChet == 75){
                                    val sumDistance = (faktEndKmChet - faktNachKmChet) + sumCalculateUslChet
                                    if (distance >= faktNachKmChet - 2000 && distance <= faktNachKmChet + sumDistance && item.speedChet == 75){
                                        val nachKm75 = faktNachKmChet
                                        val endKm75 = faktEndKmChet
                                        val speed75 = item.speedChet
                                        if (distance >= nachKm75 - 2000 && distance <= endKm75 + sumCalculateUslChet && speed75 == 75){
                                            tvOgrChet75 = "$speedChet"
                                            tvKmPkChet75 = "$titleStartChet км $piketStartChet пк - $titleFinishChet км $piketFinishChet пк"
                                        }
                                    }
                                }
                                if (distance >= faktEndKmChet + sumCalculateUslChet && distance <= faktEndKmChet + sumCalculateUslChet + 50 && item.speedChet == 75){
                                    tvOgrChet75 = ""
                                    tvKmPkChet75 = ""
                                }

                                //---------------------------------------------------------------------------------------------------------------------------------------

                                // Конец предупреждения о превышении ограничения скорости
                            }


                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            myDbManagerCustomField.openDb()
                            val dataListCustomField = myDbManagerCustomField.readDbDataCustomFieldChet()
                            for (item in dataListCustomField){

                                titleStartCustomFieldChet = item.startChetCustom
                                piketStartCustomFieldChet = item.picketStartChetCustom
                                customFieldChet = item.fieldChetCustom

                                var z = 1001
                                var kms = 2
                                val pkc = 1

                                while (z < 9999999){
                                    z += 1000
                                    kms += 1

                                    if (titleStartCustomFieldChet == kms && piketStartCustomFieldChet == pkc){
                                        faktNachKmCustomFieldChet = z
                                        piketNachKmCustomFieldChet = pkc
                                    }
                                }

                                var z1 = 1101
                                var kms1 = 2
                                val pkc1 = 2

                                while (z1 < 9999999){
                                    z1 += 1000
                                    kms1 += 1

                                    if (titleStartCustomFieldChet == kms1 && piketStartCustomFieldChet == pkc1){
                                        faktNachKmCustomFieldChet = z1
                                        piketNachKmCustomFieldChet = pkc1
                                    }
                                }

                                var z2 = 1201
                                var kms2 = 2
                                val pkc2 = 3

                                while (z2 < 9999999){
                                    z2 += 1000
                                    kms2 += 1

                                    if (titleStartCustomFieldChet == kms2 && piketStartCustomFieldChet == pkc2){
                                        faktNachKmCustomFieldChet = z2
                                        piketNachKmCustomFieldChet = pkc2
                                    }
                                }

                                var z3 = 1301
                                var kms3 = 2
                                val pkc3 = 4

                                while (z3 < 9999999){
                                    z3 += 1000
                                    kms3 += 1

                                    if (titleStartCustomFieldChet == kms3 && piketStartCustomFieldChet == pkc3){
                                        faktNachKmCustomFieldChet = z3
                                        piketNachKmCustomFieldChet = pkc3
                                    }
                                }

                                var z4 = 1401
                                var kms4 = 2
                                val pkc4 = 5

                                while (z4 < 9999999){
                                    z4 += 1000
                                    kms4 += 1

                                    if (titleStartCustomFieldChet == kms4 && piketStartCustomFieldChet == pkc4){
                                        faktNachKmCustomFieldChet = z4
                                        piketNachKmCustomFieldChet = pkc4
                                    }
                                }

                                var z5 = 1501
                                var kms5 = 2
                                val pkc5 = 6

                                while (z5 < 9999999){
                                    z5 += 1000
                                    kms5 += 1

                                    if (titleStartCustomFieldChet == kms5 && piketStartCustomFieldChet == pkc5){
                                        faktNachKmCustomFieldChet = z5
                                        piketNachKmCustomFieldChet = pkc5
                                    }
                                }

                                var z6 = 1601
                                var kms6 = 2
                                val pkc6 = 7

                                while (z6 < 9999999){
                                    z6 += 1000
                                    kms6 += 1

                                    if (titleStartCustomFieldChet == kms6 && piketStartCustomFieldChet == pkc6){
                                        faktNachKmCustomFieldChet = z6
                                        piketNachKmCustomFieldChet = pkc6
                                    }
                                }

                                var z7 = 1701
                                var kms7 = 2
                                val pkc7 = 8

                                while (z7 < 9999999){
                                    z7 += 1000
                                    kms7 += 1

                                    if (titleStartCustomFieldChet == kms7 && piketStartCustomFieldChet == pkc7){
                                        faktNachKmCustomFieldChet = z7
                                        piketNachKmCustomFieldChet = pkc7
                                    }
                                }

                                var z8 = 1801
                                var kms8 = 2
                                val pkc8 = 9

                                while (z8 < 9999999){
                                    z8 += 1000
                                    kms8 += 1

                                    if (titleStartCustomFieldChet == kms8 && piketStartCustomFieldChet == pkc8){
                                        faktNachKmCustomFieldChet = z8
                                        piketNachKmCustomFieldChet = pkc8
                                    }
                                }

                                var z9 = 1901
                                var kms9 = 2
                                val pkc9 = 10

                                while (z9 < 9999999){
                                    z9 += 1000
                                    kms9 += 1

                                    if (titleStartCustomFieldChet == kms9 && piketStartCustomFieldChet == pkc9){
                                        faktNachKmCustomFieldChet = z9
                                        piketNachKmCustomFieldChet = pkc9
                                    }
                                }
                            }
                        }


                        if (distance > faktNachKmCustomFieldChet) {
                            textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {
                                if (it == TextToSpeech.SUCCESS) {
                                    val local = Locale("us")
                                    textToSpeech.language = local
                                    textToSpeech.setSpeechRate(1.0f)
                                    textToSpeech.speak(customFieldChet, TextToSpeech.QUEUE_ADD, null)
                                }
                            })
                        }

                        myDbManagerPantograph.closeDb()
                        myDbManagerBrake.closeDb()
                        myDbManagerLimitations.closeDb()
                        myDbManagerCustomField.closeDb()

                    }


                }

                val locModel = LocationModel(
                    currentLocation.speed,
                    distance,
                    kmDistance,
                    pkDistance,
                    geoPointsTrack

                )
                sendLocData(locModel)
                Log.d("MyLog", "distance: $distance")
            }
            lastLocation = currentLocation
        }
    }

    private fun sendLocData(locModel: LocationModel){
        val i = Intent(LOC_MODEL_INTENT)
        i.putExtra(LOC_MODEL_INTENT, locModel)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun startNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nChanel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nManager = getSystemService(NotificationManager::class.java) as NotificationManager
            nManager.createNotificationChannel(nChanel)
        }
        val nIntent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(
            this,
            55,
            nIntent,
            PendingIntent.FLAG_MUTABLE
        )
        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Следуем с чётным")
            .setContentIntent(pIntent).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(105, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(105, notification)
        }
    }

    private fun initLocation(){
        locRequest = LocationRequest.create()
        locRequest.interval = 1000
        locRequest.fastestInterval = 1000
        locRequest.priority = PRIORITY_HIGH_ACCURACY
        locProvider = LocationServices.getFusedLocationProviderClient(baseContext)

    }

    private fun startLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)

            return

        locProvider.requestLocationUpdates(
            locRequest,
            locCallback,
            Looper.myLooper()
        )
    }

    private suspend fun calculationKm() = withContext(Dispatchers.Main) {
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
        var qpk = 1

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

            if (distance >= q && distance <= w){
                qpk = 1
                kmDistance = qkm
                pkDistance = qpk

                faktStartKmChet = q
                faktFinishKmChet= w
            }

            if (distance >= q1 && distance <= w1){
                qpk = 2
                kmDistance = qkm
                pkDistance = qpk

                faktStartKmChet = q1
                faktFinishKmChet = w1
            }

            if (distance >= q2 && distance <= w2){
                qpk = 3
                kmDistance = qkm
                pkDistance = qpk

                faktStartKmChet = q2
                faktFinishKmChet = w2
            }

            if (distance >= q3 && distance <= w3){
                qpk = 4
                kmDistance = qkm
                pkDistance = qpk

                faktStartKmChet = q3
                faktFinishKmChet = w3
            }

            if (distance >= q4 && distance <= w4){
                qpk = 5
                kmDistance = qkm
                pkDistance = qpk

                faktStartKmChet = q4
                faktFinishKmChet = w4
            }

            if (distance >= q5 && distance <= w5){
                qpk = 6
                kmDistance = qkm
                pkDistance = qpk

                faktStartKmChet = q5
                faktFinishKmChet = w5
            }

            if (distance >= q6 && distance <= w6){
                qpk = 7
                kmDistance = qkm
                pkDistance = qpk

                faktStartKmChet = q6
                faktFinishKmChet = w6
            }

            if (distance >= q7 && distance <= w7){
                qpk = 8
                kmDistance = qkm
                pkDistance = qpk

                faktStartKmChet = q7
                faktFinishKmChet = w7
            }

            if (distance >= q8 && distance <= w8){
                qpk = 9
                kmDistance = qkm
                pkDistance = qpk

                faktStartKmChet = q8
                faktFinishKmChet = w8
            }

            if (distance >= q9 && distance <= w9){
                qpk = 10
                kmDistance = qkm
                pkDistance = qpk

                faktStartKmChet = q9
                faktFinishKmChet = w9
            }
        }
    }

    private fun iteration1() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        for (itemMyPrev400List in myPrev400List){
                                                            for (itemLimitationsList in myLimitationList){
                                                                for (itemBrakesList in myBrakeList){
                                                                    for (itemPantographsList in myPantographList){
                                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                                            faktStartKmChet == itemBrake500 &&
                                                                            faktStartKmChet == itemPantograph800 &&
                                                                            faktStartKmChet == itemOgranichenieList &&
                                                                            faktStartKmChet == itemMyPrev400List &&
                                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                                            faktStartKmChet + 4000 == itemBrakesList &&
                                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                            neutralInsert600List.removeAt(0)
                                                                            brake500List[0] = itemBrake500 + 100
                                                                            pantograph800List[0] = itemPantograph800 + 100
                                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                                            myLimitationList[0] = itemLimitationsList + 100
                                                                            myPantographList[0] = itemPantographsList + 100
                                                                            myBrakeList[0] = itemBrakesList + 100
                                                                            isNeutralInsert600 = true
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадают 7 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemBrakesList in myBrakeList){
                                                                for (itemPantographsList in myPantographList){
                                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                                        faktStartKmChet == itemBrake500 &&
                                                                        faktStartKmChet == itemOgranichenieList &&
                                                                        faktStartKmChet == itemMyPrev400List &&
                                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                                        faktStartKmChet + 4000 == itemBrakesList &&
                                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                        neutralInsert600List.removeAt(0)
                                                                        brake500List[0] = itemBrake500 + 100
                                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                                        myLimitationList[0] = itemLimitationsList + 100
                                                                        myPantographList[0] = itemPantographsList + 100
                                                                        myBrakeList[0] = itemBrakesList + 100
                                                                        isNeutralInsert600 = true
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemBrakesList in myBrakeList){
                                                                for (itemPantographsList in myPantographList){
                                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                                        faktStartKmChet == itemPantograph800 &&
                                                                        faktStartKmChet == itemOgranichenieList &&
                                                                        faktStartKmChet == itemMyPrev400List &&
                                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                                        faktStartKmChet + 4000 == itemBrakesList &&
                                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                        neutralInsert600List.removeAt(0)
                                                                        pantograph800List[0] = itemPantograph800 + 100
                                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                                        myLimitationList[0] = itemLimitationsList + 100
                                                                        myPantographList[0] = itemPantographsList + 100
                                                                        myBrakeList[0] = itemBrakesList + 100
                                                                        isNeutralInsert600 = true
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemBrakesList in myBrakeList){
                                                                for (itemPantographsList in myPantographList){
                                                                    if (faktStartKmChet == itemBrake500 &&
                                                                        faktStartKmChet == itemPantograph800 &&
                                                                        faktStartKmChet == itemOgranichenieList &&
                                                                        faktStartKmChet == itemMyPrev400List &&
                                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                                        faktStartKmChet + 4000 == itemBrakesList &&
                                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                        brake500List.removeAt(0)
                                                                        pantograph800List[0] = itemPantograph800 + 100
                                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                                        myLimitationList[0] = itemLimitationsList + 100
                                                                        myPantographList[0] = itemPantographsList + 100
                                                                        myBrakeList[0] = itemBrakesList + 100
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 1 элемент

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                    brake500List.removeAt(0)
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                                    pantograph800List.removeAt(0)
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration2() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemBrakesList in myBrakeList){
                                                                for (itemPantographsList in myPantographList){
                                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                                        faktStartKmChet == itemBrake500 &&
                                                                        faktStartKmChet == itemPantograph800 &&
                                                                        faktStartKmChet == itemOgranichenieList &&
                                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                                        faktStartKmChet + 4000 == itemBrakesList &&
                                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                        neutralInsert600List.removeAt(0)
                                                                        brake500List[0] = itemBrake500 + 100
                                                                        pantograph800List[0] = itemPantograph800 + 100
                                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                                        myLimitationList[0] = itemLimitationsList + 100
                                                                        myPantographList[0] = itemPantographsList + 100
                                                                        myBrakeList[0] = itemBrakesList + 100
                                                                        isNeutralInsert600 = true
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 6 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    brake500List[0] = itemBrake500 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                    brake500List.removeAt(0)
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 5 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                brake500List.removeAt(0)
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                                pantograph800List.removeAt(0)
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration3() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        for (itemMyPrev400List in myPrev400List){
                                                            for (itemLimitationsList in myLimitationList){
                                                                for (itemBrakesList in myBrakeList){
                                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                                        faktStartKmChet == itemBrake500 &&
                                                                        faktStartKmChet == itemPantograph800 &&
                                                                        faktStartKmChet == itemOgranichenieList &&
                                                                        faktStartKmChet == itemMyPrev400List &&
                                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                        neutralInsert600List.removeAt(0)
                                                                        brake500List[0] = itemBrake500 + 100
                                                                        pantograph800List[0] = itemPantograph800 + 100
                                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                                        myLimitationList[0] = itemLimitationsList + 100
                                                                        myBrakeList[0] = itemBrakesList + 100
                                                                        isNeutralInsert600 = true
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //Совпадает 6 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemBrakesList in myBrakeList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    brake500List[0] = itemBrake500 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemBrakesList in myBrakeList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemBrakesList in myBrakeList){
                                                                if (faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList) {

                                                                    Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                    brake500List.removeAt(0)
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 5 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            if (faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList) {

                                                                Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                brake500List.removeAt(0)
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            if (faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList) {

                                                                Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                                pantograph800List.removeAt(0)
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration4() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        for (itemMyPrev400List in myPrev400List){
                                                            for (itemLimitationsList in myLimitationList){
                                                                for (itemPantographsList in myPantographList){
                                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                                        faktStartKmChet == itemBrake500 &&
                                                                        faktStartKmChet == itemPantograph800 &&
                                                                        faktStartKmChet == itemOgranichenieList &&
                                                                        faktStartKmChet == itemMyPrev400List &&
                                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                        neutralInsert600List.removeAt(0)
                                                                        brake500List[0] = itemBrake500 + 100
                                                                        pantograph800List[0] = itemPantograph800 + 100
                                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                                        myLimitationList[0] = itemLimitationsList + 100
                                                                        myPantographList[0] = itemPantographsList + 100
                                                                        isNeutralInsert600 = true
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 6 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    brake500List[0] = itemBrake500 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                    brake500List.removeAt(0)
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 5 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                brake500List.removeAt(0)
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                                pantograph800List.removeAt(0)
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration5() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        for (itemMyPrev400List in myPrev400List){
                                                            for (itemBrakesList in myBrakeList){
                                                                for (itemPantographsList in myPantographList){
                                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                                        faktStartKmChet == itemBrake500 &&
                                                                        faktStartKmChet == itemPantograph800 &&
                                                                        faktStartKmChet == itemOgranichenieList &&
                                                                        faktStartKmChet == itemMyPrev400List &&
                                                                        faktStartKmChet + 4000 == itemBrakesList &&
                                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                        neutralInsert600List.removeAt(0)
                                                                        brake500List[0] = itemBrake500 + 100
                                                                        pantograph800List[0] = itemPantograph800 + 100
                                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                                        myPantographList[0] = itemPantographsList + 100
                                                                        myBrakeList[0] = itemBrakesList + 100
                                                                        isNeutralInsert600 = true
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 6 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    brake500List[0] = itemBrake500 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                    brake500List.removeAt(0)
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 5 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                brake500List.removeAt(0)
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                                pantograph800List.removeAt(0)
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration6() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        for (itemMyPrev400List in myPrev400List){
                                                            for (itemLimitationsList in myLimitationList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    brake500List[0] = itemBrake500 + 100
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 5 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            if (faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList) {

                                                                Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                brake500List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        if (faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList) {

                                                            Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                            pantograph800List.removeAt(0)
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration7() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        for (itemMyPrev400List in myPrev400List){
                                                            for (itemBrakesList in myBrakeList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 4000 == itemBrakesList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    brake500List[0] = itemBrake500 + 100
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 5 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemBrakesList in myBrakeList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 4000 == itemBrakesList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemBrakesList in myBrakeList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 4000 == itemBrakesList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemBrakesList in myBrakeList){
                                                            if (faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 4000 == itemBrakesList) {

                                                                Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                brake500List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                            pantograph800List.removeAt(0)
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration8() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        for (itemMyPrev400List in myPrev400List){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemOgranichenieList &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    brake500List[0] = itemBrake500 + 100
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 5 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                brake500List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                            pantograph800List.removeAt(0)
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration9() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        for (itemMyPrev400List in myPrev400List){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemMyPrev400List in myPrev400List){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet == itemMyPrev400List) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet == itemMyPrev400List) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemMyPrev400List in myPrev400List){
                                                    if (faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet == itemMyPrev400List) {

                                                        Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                        pantograph800List.removeAt(0)
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration10() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        for (itemLimitationsList in myLimitationList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet + 2000 == itemLimitationsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemLimitationsList in myLimitationList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet + 2000 == itemLimitationsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemLimitationsList in myLimitationList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet + 2000 == itemLimitationsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemLimitationsList in myLimitationList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet + 2000 == itemLimitationsList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemLimitationsList in myLimitationList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet + 2000 == itemLimitationsList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemLimitationsList in myLimitationList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet + 2000 == itemLimitationsList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemLimitationsList in myLimitationList){
                                                    if (faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet + 2000 == itemLimitationsList) {

                                                        Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                        pantograph800List.removeAt(0)
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration11() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        for (itemBrakesList in myBrakeList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet + 4000 == itemBrakesList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                        pantograph800List.removeAt(0)
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration12() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myOgranichenieList[0] = itemOgranichenieList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                        pantograph800List.removeAt(0)
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration13() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemOgranichenieList in myOgranichenieList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemOgranichenieList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myOgranichenieList[0] = itemOgranichenieList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemOgranichenieList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        brake500List[0] = itemBrake500 + 100
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet == itemOgranichenieList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        pantograph800List[0] = itemPantograph800 + 100
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemOgranichenieList in myOgranichenieList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet == itemOgranichenieList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        pantograph800List[0] = itemPantograph800 + 100
                                                        myOgranichenieList[0] = itemOgranichenieList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 2 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                    faktStartKmChet == itemOgranichenieList) {

                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                    neutralInsert600List.removeAt(0)
                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                    isNeutralInsert600 = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                if (faktStartKmChet == itemBrake500 &&
                                                    faktStartKmChet == itemOgranichenieList) {

                                                    Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                    brake500List.removeAt(0)
                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemOgranichenieList in myOgranichenieList){
                                                if (faktStartKmChet == itemPantograph800 &&
                                                    faktStartKmChet == itemOgranichenieList) {

                                                    Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                    pantograph800List.removeAt(0)
                                                    myOgranichenieList[0] = itemOgranichenieList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration14() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemBrakesList in myBrakeList){
                                                                for (itemPantographsList in myPantographList){
                                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                                        faktStartKmChet == itemBrake500 &&
                                                                        faktStartKmChet == itemPantograph800 &&
                                                                        faktStartKmChet == itemMyPrev400List &&
                                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                                        faktStartKmChet + 4000 == itemBrakesList &&
                                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                        neutralInsert600List.removeAt(0)
                                                                        brake500List[0] = itemBrake500 + 100
                                                                        pantograph800List[0] = itemPantograph800 + 100
                                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                                        myLimitationList[0] = itemLimitationsList + 100
                                                                        myPantographList[0] = itemPantographsList + 100
                                                                        myBrakeList[0] = itemBrakesList + 100
                                                                        isNeutralInsert600 = true
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 6 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    brake500List[0] = itemBrake500 + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Торможения ближе к цели")

                                                                    brake500List.removeAt(0)
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 5 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                brake500List.removeAt(0)
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                                pantograph800List.removeAt(0)
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration15() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemBrakesList in myBrakeList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    brake500List[0] = itemBrake500 + 100
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 5 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            if (faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList) {

                                                                Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                brake500List[0] = itemBrake500 + 100
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                            pantograph800List.removeAt(0)
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration16() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    brake500List[0] = itemBrake500 + 100
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 5 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                brake500List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                            pantograph800List.removeAt(0)
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration17() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet == itemMyPrev400List &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    brake500List[0] = itemBrake500 + 100
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 5 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                brake500List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemBrakesList in myBrakeList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 4000 == itemBrakesList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemBrakesList in myBrakeList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 4000 == itemBrakesList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemBrakesList in myBrakeList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 4000 == itemBrakesList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                            pantograph800List.removeAt(0)
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration18() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemLimitationsList in myLimitationList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemLimitationsList in myLimitationList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 2000 == itemLimitationsList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 2000 == itemLimitationsList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    if (faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 2000 == itemLimitationsList) {

                                                        Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                        pantograph800List.removeAt(0)
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration19() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemBrakesList in myBrakeList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 4000 == itemBrakesList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                        pantograph800List.removeAt(0)
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration20() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemMyPrev400List in myPrev400List){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                        pantograph800List.removeAt(0)
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration21() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemMyPrev400List in myPrev400List){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet == itemMyPrev400List) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemMyPrev400List) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        brake500List[0] = itemBrake500 + 100
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet == itemMyPrev400List) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        pantograph800List[0] = itemPantograph800 + 100
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemMyPrev400List in myPrev400List){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet == itemMyPrev400List) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        pantograph800List[0] = itemPantograph800 + 100
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 2 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                    faktStartKmChet == itemMyPrev400List) {

                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                    neutralInsert600List.removeAt(0)
                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                    isNeutralInsert600 = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                if (faktStartKmChet == itemBrake500 &&
                                                    faktStartKmChet == itemMyPrev400List) {

                                                    Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                    brake500List.removeAt(0)
                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemMyPrev400List in myPrev400List){
                                                if (faktStartKmChet == itemPantograph800 &&
                                                    faktStartKmChet == itemMyPrev400List) {

                                                    Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                    pantograph800List.removeAt(0)
                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration22() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            for (itemPantographsList in myPantographList){
                                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                                    faktStartKmChet == itemBrake500 &&
                                                                    faktStartKmChet == itemPantograph800 &&
                                                                    faktStartKmChet + 2000 == itemLimitationsList &&
                                                                    faktStartKmChet + 4000 == itemBrakesList &&
                                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                    neutralInsert600List.removeAt(0)
                                                                    brake500List[0] = itemBrake500 + 100
                                                                    pantograph800List[0] = itemPantograph800 + 100
                                                                    myLimitationList[0] = itemLimitationsList + 100
                                                                    myPantographList[0] = itemPantographsList + 100
                                                                    myBrakeList[0] = itemBrakesList + 100
                                                                    isNeutralInsert600 = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 5 элементов

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                                brake500List.removeAt(0)
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemBrakesList in myBrakeList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 4000 == itemBrakesList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemBrakesList in myBrakeList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 4000 == itemBrakesList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemBrakesList in myBrakeList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 4000 == itemBrakesList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                            pantograph800List.removeAt(0)
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration23() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemLimitationsList in myLimitationList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet + 2000 == itemLimitationsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemLimitationsList in myLimitationList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet + 2000 == itemLimitationsList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        brake500List[0] = itemBrake500 + 100
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemLimitationsList in myLimitationList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet + 2000 == itemLimitationsList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        pantograph800List[0] = itemPantograph800 + 100
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemLimitationsList in myLimitationList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet + 2000 == itemLimitationsList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        pantograph800List[0] = itemPantograph800 + 100
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 2 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemLimitationsList in myLimitationList){
                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                    faktStartKmChet + 2000 == itemLimitationsList) {

                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                    neutralInsert600List.removeAt(0)
                                                    myLimitationList[0] = itemLimitationsList + 100
                                                    isNeutralInsert600 = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemLimitationsList in myLimitationList){
                                                if (faktStartKmChet == itemBrake500 &&
                                                    faktStartKmChet + 2000 == itemLimitationsList) {

                                                    Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                    brake500List.removeAt(0)
                                                    myLimitationList[0] = itemLimitationsList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemLimitationsList in myLimitationList){
                                                if (faktStartKmChet == itemPantograph800 &&
                                                    faktStartKmChet + 2000 == itemLimitationsList) {

                                                    Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                    pantograph800List.removeAt(0)
                                                    myLimitationList[0] = itemLimitationsList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration24() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemBrakesList in myBrakeList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                        pantograph800List.removeAt(0)
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration25() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemLimitationsList in myLimitationList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                        pantograph800List.removeAt(0)
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration26() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemNeutralInsert600 &&
                                                                faktStartKmChet == itemBrake500 &&
                                                                faktStartKmChet == itemPantograph800 &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                                neutralInsert600List.removeAt(0)
                                                                brake500List[0] = itemBrake500 + 100
                                                                pantograph800List[0] = itemPantograph800 + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                                isNeutralInsert600 = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 4 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemBrakesList in myBrakeList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet + 4000 == itemBrakesList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemBrakesList in myBrakeList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet + 4000 == itemBrakesList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemBrakesList in myBrakeList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet + 4000 == itemBrakesList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                            brake500List.removeAt(0)
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrakesList in myBrakeList){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet + 4000 == itemBrakesList &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        myPantographList[0] = itemPantographsList + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemBrakesList in myBrakeList){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet + 4000 == itemBrakesList &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        myPantographList[0] = itemPantographsList + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemBrakesList in myBrakeList){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet + 4000 == itemBrakesList &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                        pantograph800List.removeAt(0)
                                                        myPantographList[0] = itemPantographsList + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration27() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        brake500List[0] = itemBrake500 + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        pantograph800List[0] = itemPantograph800 + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        pantograph800List[0] = itemPantograph800 + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 2 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrakesList in myBrakeList){
                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                    faktStartKmChet + 4000 == itemBrakesList) {

                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                    neutralInsert600List.removeAt(0)
                                                    myBrakeList[0] = itemBrakesList + 100
                                                    isNeutralInsert600 = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemBrakesList in myBrakeList){
                                                if (faktStartKmChet == itemBrake500 &&
                                                    faktStartKmChet + 4000 == itemBrakesList) {

                                                    Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                    brake500List.removeAt(0)
                                                    myBrakeList[0] = itemBrakesList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemBrakesList in myBrakeList){
                                                if (faktStartKmChet == itemPantograph800 &&
                                                    faktStartKmChet + 4000 == itemBrakesList) {

                                                    Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                    pantograph800List.removeAt(0)
                                                    myBrakeList[0] = itemBrakesList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration28() {
        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantograph800 in pantograph800List) {
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemNeutralInsert600 &&
                                                            faktStartKmChet == itemBrake500 &&
                                                            faktStartKmChet == itemPantograph800 &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                            neutralInsert600List.removeAt(0)
                                                            brake500List[0] = itemBrake500 + 100
                                                            pantograph800List[0] = itemPantograph800 + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            isNeutralInsert600 = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадют 3 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemBrake500 in brake500List) {
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        brake500List[0] = itemBrake500 + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemNeutralInsert600 &&
                                                        faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                        neutralInsert600List.removeAt(0)
                                                        pantograph800List[0] = itemPantograph800 + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                        isNeutralInsert600 = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantograph800 in pantograph800List) {
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemBrake500 &&
                                                        faktStartKmChet == itemPantograph800 &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                        brake500List.removeAt(0)
                                                        pantograph800List[0] = itemPantograph800 + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 2 элемента

        if (neutralInsert600.isNotEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemNeutralInsert600 in neutralInsert600List) {
                                            for (itemPantographsList in myPantographList){
                                                if (faktStartKmChet == itemNeutralInsert600 &&
                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                    Log.d("MyLog2", "Озвучиваем Нейтральную вставку")

                                                    neutralInsert600List.removeAt(0)
                                                    myPantographList[0] = itemPantographsList + 100
                                                    isNeutralInsert600 = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isNotEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrake500 in brake500List) {
                                            for (itemPantographsList in myPantographList){
                                                if (faktStartKmChet == itemBrake500 &&
                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                    Log.d("MyLog2", "Озвучиваем Торможение ближе к цели")

                                                    brake500List.removeAt(0)
                                                    myPantographList[0] = itemPantographsList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isNotEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantograph800 in pantograph800List) {
                                            for (itemPantographsList in myPantographList){
                                                if (faktStartKmChet == itemPantograph800 &&
                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                    Log.d("MyLog2", "Озвучиваем Опускание ближе к цели")

                                                    pantograph800List.removeAt(0)
                                                    myPantographList[0] = itemPantographsList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration29() {
        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        for (itemPantographsList in myPantographList){
                                                            if (faktStartKmChet == itemOgranichenieList &&
                                                                faktStartKmChet == itemMyPrev400List &&
                                                                faktStartKmChet + 2000 == itemLimitationsList &&
                                                                faktStartKmChet + 4000 == itemBrakesList &&
                                                                faktStartKmChet + 3000 == itemPantographsList) {

                                                                Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 1")

                                                                if (isOgranichenie == 15){
                                                                    playSound(ogr15)
                                                                    isOgranichenie = 0
                                                                }
                                                                else if (isOgranichenie == 25){
                                                                    playSound(ogr25)
                                                                    isOgranichenie = 0
                                                                }
                                                                else if (isOgranichenie == 40){
                                                                    playSound(ogr40)
                                                                    isOgranichenie = 0
                                                                }
                                                                else if (isOgranichenie == 50){
                                                                    playSound(ogr50)
                                                                    isOgranichenie = 0
                                                                }
                                                                else if (isOgranichenie == 55){
                                                                    playSound(ogr55)
                                                                    isOgranichenie = 0
                                                                }
                                                                else if (isOgranichenie == 60){
                                                                    playSound(ogr60)
                                                                    isOgranichenie = 0
                                                                }
                                                                else if (isOgranichenie == 65){
                                                                    playSound(ogr65)
                                                                    isOgranichenie = 0
                                                                }
                                                                else if (isOgranichenie == 70){
                                                                    playSound(ogr70)
                                                                    isOgranichenie = 0
                                                                }
                                                                else if(isOgranichenie == 75){
                                                                    playSound(ogr75)
                                                                    isOgranichenie = 0
                                                                }

                                                                myOgranichenieList.removeAt(0)
                                                                myPrev400List[0] = itemMyPrev400List + 100
                                                                myLimitationList[0] = itemLimitationsList + 100
                                                                myPantographList[0] = itemPantographsList + 100
                                                                myBrakeList[0] = itemBrakesList + 100
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадают 4 элемента

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myPrev400List.isEmpty()){
                        if (myOgranichenieList.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemBrakesList in myBrakeList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemOgranichenieList) {

                                                            Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 3")

                                                            if (isOgranichenie == 15){
                                                                playSound(ogr15)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 25){
                                                                playSound(ogr25)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 40){
                                                                playSound(ogr40)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 50){
                                                                playSound(ogr50)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 55){
                                                                playSound(ogr55)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 60){
                                                                playSound(ogr60)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 65){
                                                                playSound(ogr65)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 70){
                                                                playSound(ogr70)
                                                                isOgranichenie = 0
                                                            }
                                                            else if(isOgranichenie == 75){
                                                                playSound(ogr75)
                                                                isOgranichenie = 0
                                                            }

                                                            myOgranichenieList.removeAt(0)
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemBrakesList in myBrakeList){
                                                        if (faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 4000 == itemBrakesList) {

                                                            Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 12")

                                                            if (isOgranichenie == 15){
                                                                playSound(ogr15)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 25){
                                                                playSound(ogr25)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 40){
                                                                playSound(ogr40)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 50){
                                                                playSound(ogr50)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 55){
                                                                playSound(ogr55)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 60){
                                                                playSound(ogr60)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 65){
                                                                playSound(ogr65)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 70){
                                                                playSound(ogr70)
                                                                isOgranichenie = 0
                                                            }
                                                            else if(isOgranichenie == 75){
                                                                playSound(ogr75)
                                                                isOgranichenie = 0
                                                            }

                                                            myOgranichenieList.removeAt(0)
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 13")

                                                            if (isOgranichenie == 15){
                                                                playSound(ogr15)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 25){
                                                                playSound(ogr25)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 40){
                                                                playSound(ogr40)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 50){
                                                                playSound(ogr50)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 55){
                                                                playSound(ogr55)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 60){
                                                                playSound(ogr60)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 65){
                                                                playSound(ogr65)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 70){
                                                                playSound(ogr70)
                                                                isOgranichenie = 0
                                                            }
                                                            else if(isOgranichenie == 75){
                                                                playSound(ogr75)
                                                                isOgranichenie = 0
                                                            }

                                                            myOgranichenieList.removeAt(0)
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemBrakesList in myBrakeList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemOgranichenieList &&
                                                            faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 4000 == itemBrakesList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 155")

                                                            if (isOgranichenie == 15){
                                                                playSound(ogr15)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 25){
                                                                playSound(ogr25)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 40){
                                                                playSound(ogr40)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 50){
                                                                playSound(ogr50)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 55){
                                                                playSound(ogr55)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 60){
                                                                playSound(ogr60)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 65){
                                                                playSound(ogr65)
                                                                isOgranichenie = 0
                                                            }
                                                            else if (isOgranichenie == 70){
                                                                playSound(ogr70)
                                                                isOgranichenie = 0
                                                            }
                                                            else if(isOgranichenie == 75){
                                                                playSound(ogr75)
                                                                isOgranichenie = 0
                                                            }

                                                            myOgranichenieList.removeAt(0)
                                                            myPrev400List[0] = itemMyPrev400List + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадают 3 элемента

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemLimitationsList in myLimitationList){
                                                    if (faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 2000 == itemLimitationsList) {

                                                        Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 4")

                                                        if (isOgranichenie == 15){
                                                            playSound(ogr15)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 25){
                                                            playSound(ogr25)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 40){
                                                            playSound(ogr40)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 50){
                                                            playSound(ogr50)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 55){
                                                            playSound(ogr55)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 60){
                                                            playSound(ogr60)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 65){
                                                            playSound(ogr65)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 70){
                                                            playSound(ogr70)
                                                            isOgranichenie = 0
                                                        }
                                                        else if(isOgranichenie == 75){
                                                            playSound(ogr75)
                                                            isOgranichenie = 0
                                                        }

                                                        myOgranichenieList.removeAt(0)
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myBrakeList.isNotEmpty()){
                                if (myLimitationList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemBrakesList in myBrakeList){
                                                    if (faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 4000 == itemBrakesList) {

                                                        Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 5")

                                                        if (isOgranichenie == 15){
                                                            playSound(ogr15)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 25){
                                                            playSound(ogr25)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 40){
                                                            playSound(ogr40)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 50){
                                                            playSound(ogr50)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 55){
                                                            playSound(ogr55)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 60){
                                                            playSound(ogr60)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 65){
                                                            playSound(ogr65)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 70){
                                                            playSound(ogr70)
                                                            isOgranichenie = 0
                                                        }
                                                        else if(isOgranichenie == 75){
                                                            playSound(ogr75)
                                                            isOgranichenie = 0
                                                        }

                                                        myOgranichenieList.removeAt(0)
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myPantographList.isNotEmpty()){
                                if (myLimitationList.isEmpty()){
                                    if (myBrakeList.isEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            for (itemMyPrev400List in myPrev400List){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemOgranichenieList &&
                                                        faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 6")

                                                        if (isOgranichenie == 15){
                                                            playSound(ogr15)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 25){
                                                            playSound(ogr25)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 40){
                                                            playSound(ogr40)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 50){
                                                            playSound(ogr50)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 55){
                                                            playSound(ogr55)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 60){
                                                            playSound(ogr60)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 65){
                                                            playSound(ogr65)
                                                            isOgranichenie = 0
                                                        }
                                                        else if (isOgranichenie == 70){
                                                            playSound(ogr70)
                                                            isOgranichenie = 0
                                                        }
                                                        else if(isOgranichenie == 75){
                                                            playSound(ogr75)
                                                            isOgranichenie = 0
                                                        }

                                                        myOgranichenieList.removeAt(0)
                                                        myPrev400List[0] = itemMyPrev400List + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадают 2 элемента

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            for (itemMyPrev400List in myPrev400List){
                                                if (faktStartKmChet == itemOgranichenieList &&
                                                    faktStartKmChet == itemMyPrev400List) {

                                                    Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 7")

                                                    if (isOgranichenie == 15){
                                                        playSound(ogr15)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 25){
                                                        playSound(ogr25)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 40){
                                                        playSound(ogr40)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 50){
                                                        playSound(ogr50)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 55){
                                                        playSound(ogr55)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 60){
                                                        playSound(ogr60)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 65){
                                                        playSound(ogr65)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 70){
                                                        playSound(ogr70)
                                                        isOgranichenie = 0
                                                    }
                                                    else if(isOgranichenie == 75){
                                                        playSound(ogr75)
                                                        isOgranichenie = 0
                                                    }

                                                    myOgranichenieList.removeAt(0)
                                                    myPrev400List[0] = itemMyPrev400List + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            for (itemLimitationsList in myLimitationList){
                                                if (faktStartKmChet == itemOgranichenieList &&
                                                    faktStartKmChet + 2000 == itemLimitationsList) {

                                                    Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 8")

                                                    if (isOgranichenie == 15){
                                                        playSound(ogr15)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 25){
                                                        playSound(ogr25)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 40){
                                                        playSound(ogr40)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 50){
                                                        playSound(ogr50)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 55){
                                                        playSound(ogr55)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 60){
                                                        playSound(ogr60)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 65){
                                                        playSound(ogr65)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 70){
                                                        playSound(ogr70)
                                                        isOgranichenie = 0
                                                    }
                                                    else if(isOgranichenie == 75){
                                                        playSound(ogr75)
                                                        isOgranichenie = 0
                                                    }

                                                    myOgranichenieList.removeAt(0)
                                                    myLimitationList[0] = itemLimitationsList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            for (itemBrakesList in myBrakeList){
                                                if (faktStartKmChet == itemOgranichenieList &&
                                                    faktStartKmChet + 4000 == itemBrakesList) {

                                                    Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 8")

                                                    if (isOgranichenie == 15){
                                                        playSound(ogr15)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 25){
                                                        playSound(ogr25)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 40){
                                                        playSound(ogr40)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 50){
                                                        playSound(ogr50)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 55){
                                                        playSound(ogr55)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 60){
                                                        playSound(ogr60)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 65){
                                                        playSound(ogr65)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 70){
                                                        playSound(ogr70)
                                                        isOgranichenie = 0
                                                    }
                                                    else if(isOgranichenie == 75){
                                                        playSound(ogr75)
                                                        isOgranichenie = 0
                                                    }

                                                    myOgranichenieList.removeAt(0)
                                                    myBrakeList[0] = itemBrakesList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isNotEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            for (itemPantographsList in myPantographList){
                                                if (faktStartKmChet == itemOgranichenieList &&
                                                    faktStartKmChet + 3000 == itemPantographsList) {

                                                    Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 9")

                                                    if (isOgranichenie == 15){
                                                        playSound(ogr15)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 25){
                                                        playSound(ogr25)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 40){
                                                        playSound(ogr40)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 50){
                                                        playSound(ogr50)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 55){
                                                        playSound(ogr55)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 60){
                                                        playSound(ogr60)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 65){
                                                        playSound(ogr65)
                                                        isOgranichenie = 0
                                                    }
                                                    else if (isOgranichenie == 70){
                                                        playSound(ogr70)
                                                        isOgranichenie = 0
                                                    }
                                                    else if(isOgranichenie == 75){
                                                        playSound(ogr75)
                                                        isOgranichenie = 0
                                                    }

                                                    myOgranichenieList.removeAt(0)
                                                    myPantographList[0] = itemPantographsList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 1 элемента

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myPrev400List.isEmpty()){
                        if (myLimitationList.isEmpty()){
                            if (myBrakeList.isEmpty()){
                                if (myPantographList.isEmpty()){
                                    if (myOgranichenieList.isNotEmpty()){
                                        for (itemOgranichenieList in myOgranichenieList){
                                            if (faktStartKmChet == itemOgranichenieList){

                                                Log.d("MyLog1", "Озвучиваем Ограничение скорости Следование по предупреждению! 2")

                                                if (isOgranichenie == 15){
                                                    playSound(ogr15)
                                                    isOgranichenie = 0
                                                }
                                                else if (isOgranichenie == 25){
                                                    playSound(ogr25)
                                                    isOgranichenie = 0
                                                }
                                                else if (isOgranichenie == 40){
                                                    playSound(ogr40)
                                                    isOgranichenie = 0
                                                }
                                                else if (isOgranichenie == 50){
                                                    playSound(ogr50)
                                                    isOgranichenie = 0
                                                }
                                                else if (isOgranichenie == 55){
                                                    playSound(ogr55)
                                                    isOgranichenie = 0
                                                }
                                                else if (isOgranichenie == 60){
                                                    playSound(ogr60)
                                                    isOgranichenie = 0
                                                }
                                                else if (isOgranichenie == 65){
                                                    playSound(ogr65)
                                                    isOgranichenie = 0
                                                }
                                                else if (isOgranichenie == 70){
                                                    playSound(ogr70)
                                                    isOgranichenie = 0
                                                }
                                                else if(isOgranichenie == 75){
                                                    playSound(ogr75)
                                                    isOgranichenie = 0
                                                }

                                                myOgranichenieList.removeAt(0)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадение превышения за 400 метров с остальными звуками
        // Совпадает 4 элемента

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemMyPrev400List in myPrev400List){
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemBrakesList in myBrakeList){
                                                    for (itemPantographsList in myPantographList){
                                                        if (faktStartKmChet == itemMyPrev400List &&
                                                            faktStartKmChet + 2000 == itemLimitationsList &&
                                                            faktStartKmChet + 4000 == itemBrakesList &&
                                                            faktStartKmChet + 3000 == itemPantographsList) {

                                                            Log.d("MyLog1", "Озвучивание превышения скорости за 400 метров! 2")

                                                            playSound(voiceprev)

                                                            myPrev400List.removeAt(0)
                                                            myLimitationList[0] = itemLimitationsList + 100
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 3 элемента

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemMyPrev400List in myPrev400List){
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemBrakesList in myBrakeList) {
                                                    if (faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                        faktStartKmChet + 4000 == itemBrakesList){

                                                        Log.d("MyLog1", "Озвучивание превышения скорости за 400 метров! 31")

                                                        playSound(voiceprev)

                                                        myPrev400List.removeAt(0)
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemMyPrev400List in myPrev400List){
                                            for (itemLimitationsList in myLimitationList){
                                                for (itemPantographsList in myPantographList) {
                                                    if (faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 2000 == itemLimitationsList &&
                                                        faktStartKmChet + 3000 == itemPantographsList){

                                                        Log.d("MyLog1", "Озвучивание превышения скорости за 400 метров! 32")

                                                        playSound(voiceprev)

                                                        myPrev400List.removeAt(0)
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        myPantographList[0] = itemPantographsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemMyPrev400List in myPrev400List){
                                            for (itemBrakesList in myBrakeList){
                                                for (itemPantographsList in myPantographList){
                                                    if (faktStartKmChet == itemMyPrev400List &&
                                                        faktStartKmChet + 4000 == itemBrakesList &&
                                                        faktStartKmChet + 3000 == itemPantographsList) {

                                                        Log.d("MyLog1", "Озвучивание превышения скорости за 400 метров! 156")

                                                        playSound(voiceprev)

                                                        myPrev400List.removeAt(0)
                                                        myPantographList[0] = itemPantographsList + 100
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 2 элемента

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemMyPrev400List in myPrev400List){
                                            for (itemLimitationsList in myLimitationList){
                                                if (faktStartKmChet == itemMyPrev400List &&
                                                    faktStartKmChet + 2000 == itemLimitationsList){

                                                    Log.d("MyLog1", "Озвучивание превышения скорости за 400 метров! 3")

                                                    playSound(voiceprev)

                                                    myPrev400List.removeAt(0)
                                                    myLimitationList[0] = itemLimitationsList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemMyPrev400List in myPrev400List){
                                            for (itemBrakesList in myBrakeList){
                                                if (faktStartKmChet == itemMyPrev400List &&
                                                    faktStartKmChet + 4000 == itemBrakesList){

                                                    Log.d("MyLog1", "Озвучивание превышения скорости за 400 метров! 4")

                                                    playSound(voiceprev)

                                                    myPrev400List.removeAt(0)
                                                    myBrakeList[0] = itemBrakesList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isNotEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemMyPrev400List in myPrev400List){
                                            for (itemPantographsList in myPantographList){
                                                if (faktStartKmChet == itemMyPrev400List &&
                                                    faktStartKmChet + 3000 == itemPantographsList){

                                                    Log.d("MyLog1", "Озвучивание превышения скорости за 400 метров! 5")

                                                    playSound(voiceprev)

                                                    myPrev400List.removeAt(0)
                                                    myPantographList[0] = itemPantographsList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Совпадает 1 элемент

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myLimitationList.isEmpty()){
                            if (myBrakeList.isEmpty()){
                                if (myPantographList.isEmpty()){
                                    if (myPrev400List.isNotEmpty()){
                                        for (itemMyPrev400List in myPrev400List){
                                            if (faktStartKmChet == itemMyPrev400List){

                                                Log.d("MyLog1", "Озвучивание превышения скорости за 400 метров! 1")

                                                playSound(voiceprev)

                                                myPrev400List.removeAt(0)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iteration30() {
        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemLimitationsList in myLimitationList){
                                            for (itemBrakesList in myBrakeList) {
                                                for (itemPantographsList in myPantographList) {

                                                    Log.d("MyLog2", "Ограничение!")

                                                    if (itemLimitationsList == itemBrakesList - 2100 &&  itemLimitationsList == itemPantographsList - 1100) {
                                                        myLimitationList[0] = itemLimitationsList + 100
                                                        Log.d("MyLog2", "Ограничение но не надо тут ни чего делать сейчас!")
                                                    }

                                                    if (faktStartKmChet + 2000 == itemLimitationsList && faktStartKmChet + 4000 == itemBrakesList && faktStartKmChet + 3000 == itemPantographsList) {
                                                        Log.d("MyLog6", "Озвучиваем Ограничение скорости! 111112")

                                                        if (mySpeed.isNotEmpty()) {
                                                            if (mySpeed[0] == 15) {
                                                                playSound(voice15)
                                                            }
                                                            else if (mySpeed[0] == 25) {
                                                                playSound(voice25)
                                                            }
                                                            else if (mySpeed[0] == 40) {
                                                                playSound(voice40)
                                                            }
                                                            else if (mySpeed[0] == 50) {
                                                                playSound(voice50)
                                                            }
                                                            else if (mySpeed[0] == 55) {
                                                                playSound(voice55)
                                                            }
                                                            else if (mySpeed[0] == 60) {
                                                                playSound(voice60)
                                                            }
                                                            else if (mySpeed[0] == 65) {
                                                                playSound(voice65)
                                                            }
                                                            else if (mySpeed[0] == 70) {
                                                                playSound(voice70)
                                                            }
                                                            else if (mySpeed[0] == 75) {
                                                                playSound(voice75)
                                                            }

                                                            mySpeed.removeAt(0)
                                                            myLimitationList.removeAt(0)
                                                            myPantographList[0] = itemPantographsList + 100
                                                            myBrakeList[0] = itemBrakesList + 100
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myPantographList.isEmpty()){
                                if (myBrakeList.isEmpty()){
                                    if (myLimitationList.isNotEmpty()){
                                        for (itemLimitationsList in myLimitationList){
                                            if (faktStartKmChet + 2000 == itemLimitationsList){

                                                Log.d("MyLog5", "Озвучиваем Ограничение скорости! 4")
                                                Log.d("MyLog", "speedChetMin => => => $speedChetMin")

                                                if (mySpeed.isNotEmpty()) {
                                                    if (mySpeed[0] == 15) {
                                                        playSound(voice15)
                                                    }
                                                    else if (mySpeed[0] == 25) {
                                                        playSound(voice25)
                                                    }
                                                    else if (mySpeed[0] == 40) {
                                                        playSound(voice40)
                                                    }
                                                    else if (mySpeed[0] == 50) {
                                                        playSound(voice50)
                                                    }
                                                    else if (mySpeed[0] == 55) {
                                                        playSound(voice55)
                                                    }
                                                    else if (mySpeed[0] == 60) {
                                                        playSound(voice60)
                                                    }
                                                    else if (mySpeed[0] == 65) {
                                                        playSound(voice65)
                                                    }
                                                    else if (mySpeed[0] == 70) {
                                                        playSound(voice70)
                                                    }
                                                    else if (mySpeed[0] == 75) {
                                                        playSound(voice75)
                                                    }

                                                    mySpeed.removeAt(0)
                                                    myLimitationList.removeAt(0)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isEmpty()){
                                        for (itemLimitationsList in myLimitationList){
                                            for (itemBrakesList in myBrakeList){
                                                if (faktStartKmChet + 2000 == itemLimitationsList && faktStartKmChet + 4000 == itemBrakesList){
                                                    Log.d("MyLog", "Озвучиваем Ограничение скорости! 5")

                                                    if (mySpeed.isNotEmpty()) {
                                                        if (mySpeed[0] == 15) {
                                                            playSound(voice15)
                                                        }
                                                        else if (mySpeed[0] == 25) {
                                                            playSound(voice25)
                                                        }
                                                        else if (mySpeed[0] == 40) {
                                                            playSound(voice40)
                                                        }
                                                        else if (mySpeed[0] == 50) {
                                                            playSound(voice50)
                                                        }
                                                        else if (mySpeed[0] == 55) {
                                                            playSound(voice55)
                                                        }
                                                        else if (mySpeed[0] == 60) {
                                                            playSound(voice60)
                                                        }
                                                        else if (mySpeed[0] == 65) {
                                                            playSound(voice65)
                                                        }
                                                        else if (mySpeed[0] == 70) {
                                                            playSound(voice70)
                                                        }
                                                        else if (mySpeed[0] == 75) {
                                                            playSound(voice75)
                                                        }

                                                        mySpeed.removeAt(0)
                                                        myLimitationList.removeAt(0)
                                                        myBrakeList[0] = itemBrakesList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isNotEmpty()){
                                if (myPantographList.isNotEmpty()){
                                    if (myBrakeList.isEmpty()){
                                        for (itemLimitationsList in myLimitationList){
                                            for (itemPantographsList in myPantographList){
                                                if (faktStartKmChet + 2000 == itemLimitationsList && faktStartKmChet + 3000 == itemPantographsList){
                                                    Log.d("MyLog", "Озвучиваем Ограничение скорости! 6")

                                                    if (mySpeed.isNotEmpty()) {
                                                        if (mySpeed[0] == 15) {
                                                            playSound(voice15)
                                                        }
                                                        else if (mySpeed[0] == 25) {
                                                            playSound(voice25)
                                                        }
                                                        else if (mySpeed[0] == 40) {
                                                            playSound(voice40)
                                                        }
                                                        else if (mySpeed[0] == 50) {
                                                            playSound(voice50)
                                                        }
                                                        else if (mySpeed[0] == 55) {
                                                            playSound(voice55)
                                                        }
                                                        else if (mySpeed[0] == 60) {
                                                            playSound(voice60)
                                                        }
                                                        else if (mySpeed[0] == 65) {
                                                            playSound(voice65)
                                                        }
                                                        else if (mySpeed[0] == 70) {
                                                            playSound(voice70)
                                                        }
                                                        else if (mySpeed[0] == 75) {
                                                            playSound(voice75)
                                                        }

                                                        mySpeed.removeAt(0)
                                                        myLimitationList.removeAt(0)
                                                        myPantographList[0] = itemPantographsList + 100
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myBrakeList.isNotEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemBrakesList in myBrakeList){
                                            for (itemPantographsList in myPantographList){
                                                Log.d("MyLog2", "Опускание и Торможение!")
                                                if (mySpeed.isNotEmpty()) {
                                                    var limit = itemPantographsList - 1000
                                                    myLimitationList.add(limit)
                                                }
                                                else if (mySpeed.isEmpty() && faktStartKmChet + 3000 == itemPantographsList && faktStartKmChet + 4000 == itemBrakesList){
                                                    Log.d("MyLog", "Совпадение Опускания с Торможением!")
                                                    Log.d("MyLog", "Озвучиваем Опускание! 21")

                                                    playSound(pantograph)

                                                    myPantographList.removeAt(0)
                                                    myBrakeList[0] = itemBrakesList + 100
                                                }
                                            }
                                        }
                                    }
                                }
                                else if (myBrakeList.isEmpty()){
                                    if (myPantographList.isNotEmpty()){
                                        for (itemPantographsList in myPantographList){
                                            Log.d("MyLog2", "Опускание!")
                                            if (mySpeed.isNotEmpty()) {
                                                var limit = itemPantographsList - 1000
                                                myLimitationList.add(limit)
                                            }
                                            else if (mySpeed.isEmpty() && faktStartKmChet + 3000 == itemPantographsList){
                                                Log.d("MyLog", "Озвучиваем Опускание! 2121")

                                                playSound(pantograph)

                                                myPantographList.removeAt(0)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (neutralInsert600.isEmpty()) {
            if (brake500.isEmpty()) {
                if (pantograph800.isEmpty()) {
                    if (myOgranichenieList.isEmpty()){
                        if (myPrev400List.isEmpty()){
                            if (myLimitationList.isEmpty()){
                                if (myPantographList.isEmpty()){
                                    if (myBrakeList.isNotEmpty()){
                                        for (itemBrakesList in myBrakeList){
                                            Log.d("MyLog2", "Торможение!")
                                            if (mySpeed.isNotEmpty()) {
                                                var limit = itemBrakesList - 2000
                                                myLimitationList.add(limit)
                                            }
                                            else if (mySpeed.isEmpty() && faktStartKmChet + 4000 == itemBrakesList) {
                                                Log.d("MyLog", "Озвучиваем Торможение!22")

                                                playSound(brake)

                                                myBrakeList.removeAt(0)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        locProvider.removeLocationUpdates(locCallback)
//        LocalBroadcastManager.getInstance(applicationContext as AppCompatActivity)
//            .unregisterReceiver(receiverLatLongKmToService)
    }

    companion object{

        const val LOC_MODEL_INTENT = "loc_model_intent"
        const val CHANNEL_ID = "channel_1"
        var isRunning = false
    }
}