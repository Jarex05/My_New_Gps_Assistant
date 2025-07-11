package com.mikhail_ryumkin_r.my_gps_assistant.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Switch
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mikhail_ryumkin_r.my_gps_assistant.MainApp
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentRecordingARouteBinding
import com.mikhail_ryumkin_r.my_gps_assistant.dbRoom.TrackItemChet
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel.LocationModelRecording
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationService.MyLocationServiceRecording
import com.mikhail_ryumkin_r.my_gps_assistant.mainViewModels.MainViewModel
import com.mikhail_ryumkin_r.my_gps_assistant.utils.DialogManager
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import com.mikhail_ryumkin_r.my_gps_assistant.utils.checkPermission
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import com.mikhail_ryumkin_r.my_gps_assistant.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
//import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.getValue

@Suppress("DEPRECATION")
class RecordingARouteFragment : Fragment() {
    private var pl: Polyline? = null
    private var sbL = StringBuilder("")

    private var isServiceRunningRecording = false
    private lateinit var pLauncherRecording: ActivityResultLauncher<Array<String>>
    private var locationModelRecording: LocationModelRecording? = null
    private var firstStartRecording = true
    private lateinit var mLocOverlay: MyLocationNewOverlay

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchRecording: Switch
    private lateinit var binding: FragmentRecordingARouteBinding

    private val modelRecording: MainViewModel by activityViewModels{
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsmRecording()
        binding = FragmentRecordingARouteBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun settingsOsmRecording() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref_recording", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun initOsmRecording() = with(binding){

        pl = Polyline()
        pl?.outlinePaint?.color = Color.BLUE
        mapRecording.controller.setZoom(20.0)
        val mLocProvider = GpsMyLocationProvider(activity)
        mLocOverlay = MyLocationNewOverlay(mLocProvider, mapRecording)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation()
        mLocOverlay.runOnFirstFix {
            mapRecording.overlays.clear()
            mapRecording.overlays.add(pl)
            mapRecording.overlays.add(mLocOverlay)
        }
    }
    private fun registerPermissionsRecording(){
        pLauncherRecording = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
            if(it[Manifest.permission.ACCESS_FINE_LOCATION] == true || it[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true){
                initOsmRecording()
                checkLocationEnabledRecording()
            } else {
                showToast("Вы не дали разрешения на использование местоположения!")
            }
        }
    }

    private fun checkLocPermissionRecording(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            checkPermissionAfter10Recording()
        } else {
            checkPermissionBefore10Recording()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionAfter10Recording() {
        val hasForegroundLocationPermission
                = ContextCompat.checkSelfPermission(requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasForegroundLocationPermission){
            val hasBackgroundLocationPermission
                    = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (hasBackgroundLocationPermission){
                initOsmRecording()
                checkLocationEnabledRecording()
            } else {
                pLauncherRecording.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                )
            }
        } else {
            pLauncherRecording.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun checkPermissionBefore10Recording() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            initOsmRecording()
            checkLocationEnabledRecording()
        } else {
            pLauncherRecording.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun checkLocationEnabledRecording(){
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled){
            showToast("Геолокация не включена")
            DialogManager.showLocEnableDialogRecording(
                activity as AppCompatActivity,
                object : DialogManager.ListenerRecording{
                    override fun onClickRecording() {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }

                }
            )
        } else {
            showToast("Геолокация включена")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerPermissionsRecording()
        setOnClicksRecording()
        checkServiceStateRecording()
        registerLocReceiverRecording()
        locationUpdateRecording()

        switchRecording = binding.switch1Recording

        val v = SharedPref.Companion.getValueRecording(requireActivity())

        if (v != 0) {
            switchRecording.isChecked = true
        } else {
            switchRecording.isChecked = false
        }

        switchMainRecording()
    }

    private fun setOnClicksRecording() = with(binding){
        val listenerRecording = onClicksRecording()
        fStartStopRecording.setOnClickListener(listenerRecording)
        fCenterRecording.setOnClickListener(listenerRecording)
    }

    private fun onClicksRecording(): OnClickListener {
        return OnClickListener {
            when(it.id) {
                R.id.fStartStopRecording -> startStopServiceRecording()
                R.id.fCenterRecording -> centerLocationRecording()
            }
        }
    }

    private fun centerLocationRecording(){
        binding.mapRecording.controller.animateTo(mLocOverlay.myLocation)
        mLocOverlay.enableFollowLocation()
    }

    @SuppressLint("DefaultLocale")
    private fun locationUpdateRecording() = with(binding){
        modelRecording.locationUpdatesRecording.observe(viewLifecycleOwner){
            val distanceRecording = "${String.format("%.3f", it.distance1Recording / 1000.0f)} км"
            sbL = it.sbL
//            val kmPkRecording = "${it.kmDistanceRecording} км ${it.pkDistanceRecording} пк"
            val speedRecording = "${String.format("%.0f", 3.6f * it.speedRecording)} км/h"
            updatePolylineRecording(it.geoPointsList)
            tvDistanceRecording.text = distanceRecording
            tvSpeedRecording.text = speedRecording
//            tvKmPkRecording.text = kmPkRecording
            locationModelRecording = it
        }
    }

    private fun geoPointsToString(list: List<GeoPoint>): String {
        val sb = StringBuilder()
        val sbL = StringBuilder(sbL)

        var lastDistance = 0f
        list.forEachIndexed { index, item ->
            lastDistance = if (index == 0) {
                0f
            } else {
                val loc = Location("")
                val loc2 = Location("")
                loc.latitude = list[index - 1].latitude
                loc.longitude = list[index - 1].longitude

                loc2.latitude = item.latitude
                loc2.longitude = item.longitude
                lastDistance + loc.distanceTo(loc2)
            }

            sb.append("${item.latitude}, ${item.longitude}, $lastDistance /")

        }
        Log.d("MyLog", "Координаты $sbL")
        return sbL.toString()
    }

    @SuppressLint("ImplicitSamInstance")
    private fun startStopServiceRecording() {
        if (!isServiceRunningRecording && !MyLocationServiceRecording.isRunningRecording){
            startLocServiceRecording()
        } else {
            switchRecording.isGone = switchRecording.isVisible
            binding.mapRecording.isVisible = binding.mapRecording.isGone
            binding.fCenterRecording.isVisible = binding.fCenterRecording.isGone
            binding.tvDistanceRecording.isVisible = binding.tvDistanceRecording.isGone
            binding.tvSpeedRecording.isVisible = binding.tvSpeedRecording.isGone
            binding.tvKmPkRecording.isVisible = binding.tvKmPkRecording.isGone
            val track = getTrackItem()
            DialogManager.chowSaveDialog(requireContext(),
                track,
                object : DialogManager.ListenerTrack{
                    override fun onClickTrack() {
                        showToast("Маршрут сохранён!")
                        CoroutineScope(Dispatchers.IO).launch {
                            modelRecording.insertTrack(track)
                        }
                    }

                })

            activity?.stopService(Intent(activity, MyLocationServiceRecording::class.java))
            binding.fStartStopRecording.setImageResource(R.drawable.is_play_recording)

        }
        isServiceRunningRecording = !isServiceRunningRecording
    }

    @SuppressLint("DefaultLocale")
    private fun getTrackItem(): TrackItemChet{
        return TrackItemChet(
            null,
            "",
            String.format("%.3f", locationModelRecording?.distance1Recording?.div(1000.0f) ?: 0.0f),
            geoPointsToString((locationModelRecording?.geoPointsList ?: listOf()))

        )
    }

    private fun checkServiceStateRecording(){
        isServiceRunningRecording = MyLocationServiceRecording.isRunningRecording
        switchRecording = binding.switch1Recording
        if (isServiceRunningRecording){
            binding.fStartStopRecording.setImageResource(R.drawable.is_stop_recording)
            switchRecording.isVisible = switchRecording.isGone
            binding.mapRecording.isGone = binding.mapRecording.isVisible
            binding.fCenterRecording.isGone = binding.fCenterRecording.isVisible
            binding.tvDistanceRecording.isGone = binding.tvDistanceRecording.isVisible
            binding.tvSpeedRecording.isGone = binding.tvSpeedRecording.isVisible
            binding.tvKmPkRecording.isGone = binding.tvKmPkRecording.isVisible
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun startLocServiceRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            activity?.startForegroundService(Intent(activity, MyLocationServiceRecording::class.java))
        }else{
            activity?.startService(Intent(activity, MyLocationServiceRecording::class.java))
        }
        binding.fStartStopRecording.setImageResource(R.drawable.is_stop_recording)
        switchRecording.isVisible = switchRecording.isGone
        binding.mapRecording.isGone = binding.mapRecording.isVisible
        binding.fCenterRecording.isGone = binding.fCenterRecording.isVisible
        binding.tvDistanceRecording.isGone = binding.tvDistanceRecording.isVisible
        binding.tvSpeedRecording.isGone = binding.tvSpeedRecording.isVisible
        binding.tvKmPkRecording.isGone = binding.tvKmPkRecording.isVisible
    }

    override fun onResume() {
        super.onResume()
        checkLocPermissionRecording()
        firstStartRecording = true
    }

    override fun onPause() {
        super.onPause()

        switchRecording = binding.switch1Recording

        val v = SharedPref.Companion.getValueRecording(requireActivity())

        if (v != 0) {
            switchRecording.isChecked = true
        } else {
            switchRecording.isChecked = false
        }
    }

    private fun switchMainRecording() {
        switchRecording = binding.switch1Recording

        switchRecording.setOnClickListener {
            if (switchRecording.isChecked) {
                SharedPref.Companion.setValueRecording(requireActivity(), 1)
            } else {
                SharedPref.Companion.setValueRecording(requireActivity(), 0)
            }
        }
    }

    private val receiverRecording = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MyLocationServiceRecording.LOC_MODEL_INTENT_RECORDING){
                val locModel = intent.getSerializableExtra(MyLocationServiceRecording.LOC_MODEL_INTENT_RECORDING) as LocationModelRecording
                modelRecording.locationUpdatesRecording.value = locModel
            }
        }
    }

    private fun registerLocReceiverRecording(){
        val locFilter = IntentFilter(MyLocationServiceRecording.LOC_MODEL_INTENT_RECORDING)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .registerReceiver(receiverRecording, locFilter)
    }

    private fun addPointRecording(list: List<GeoPoint>){
        if (list.isNotEmpty()) pl?.addPoint(list[list.size - 1])
    }

    private fun fillPolylineRecording(list: List<GeoPoint>){
        list.forEach {
            pl?.addPoint(it)
        }
    }

    private fun updatePolylineRecording(list: List<GeoPoint>){
        if (list.size > 1 && firstStartRecording){
            fillPolylineRecording(list)
            firstStartRecording = false
        } else {
            addPointRecording(list)
        }
    }

    override fun onDetach() {
        super.onDetach()
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .unregisterReceiver(receiverRecording)
    }

    companion object {
        @JvmStatic
        fun newInstance() = RecordingARouteFragment()
    }
}