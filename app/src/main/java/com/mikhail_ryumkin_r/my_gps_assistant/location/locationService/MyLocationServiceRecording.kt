package com.mikhail_ryumkin_r.my_gps_assistant.location.locationService

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.preference.PreferenceManager
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
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.RecordingARouteFragment
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel.LocationModel
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel.LocationModelRecording
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import org.osmdroid.util.GeoPoint

@Suppress("DEPRECATION")
class MyLocationServiceRecording : Service() {
    private var sbL = StringBuilder()
    private var distance1Recording: Float = 0.0f
    private var distance2Recording: Float = 0.0f
    private lateinit var geoPointsList: ArrayList<GeoPoint>
    private var isDebag = true
    var kmDistance: Int = 0
    var pkDistance: Int = 0

    private var lastLocationRecording: Location? = null
    private lateinit var locProviderRecording: FusedLocationProviderClient
    private lateinit var locRequestRecording: LocationRequest

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        distance1Recording = SharedPref.Companion.getValueKmAddd(baseContext).toFloat()
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
                    } else {
                        distance1Recording += lastLocationRecording?.distanceTo(currentLocationRecording)!!
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nChanel = NotificationChannel(
                CHANNEL_ID_RECORDING,
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
            CHANNEL_ID_RECORDING
        ).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Запись маршрута с Чётным поездом")
            .setContentIntent(pIntent).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(106, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(106, notification)
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

    companion object{
        const val LOC_MODEL_INTENT_RECORDING = "loc_model_intent_recording"
        const val CHANNEL_ID_RECORDING = "channel_1_Recording"
        var isRunningRecording = false
    }
}
