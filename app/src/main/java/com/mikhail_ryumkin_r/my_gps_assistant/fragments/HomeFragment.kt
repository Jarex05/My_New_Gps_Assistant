package com.mikhail_ryumkin_r.my_gps_assistant.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentHomeBinding
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel.FragmentLatLongKmToServiceModel
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationModel.LocationModel
import com.mikhail_ryumkin_r.my_gps_assistant.location.locationService.MyLocationService
import com.mikhail_ryumkin_r.my_gps_assistant.mainViewModels.MainViewModel
import com.mikhail_ryumkin_r.my_gps_assistant.utils.DialogManager
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import com.mikhail_ryumkin_r.my_gps_assistant.utils.checkPermission
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import com.mikhail_ryumkin_r.my_gps_assistant.BuildConfig
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.customField.CustomFieldNechetFragment
import com.mikhail_ryumkin_r.my_gps_assistant.utils.openFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
//import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.getValue
import kotlin.text.toDouble

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {
    private var polyline = Polyline()
    private var polyline1 = Polyline()
    private var isServiceRunning = false
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private var locationModel: LocationModel? = null
    private val distanceArray = ArrayList<Float>()
    private var firstStart = true
    private lateinit var mLocOverlay: MyLocationNewOverlay

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switch: Switch
    private lateinit var binding: FragmentHomeBinding

    private val model: MainViewModel by activityViewModels{
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
            settingsOsm()
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    private fun settingsOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun registerPermissions(){
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
            if(it[Manifest.permission.ACCESS_FINE_LOCATION] == true || it[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true){
                getTrack()
                checkLocationEnabled()
            } else {
                showToast("Вы не дали разрешения на использование местоположения!")
            }
        }
    }

    private fun checkLocPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            checkPermissionAfter10()
        } else {
            checkPermissionBefore10()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionAfter10() {
        val hasForegroundLocationPermission
                = ContextCompat.checkSelfPermission(requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasForegroundLocationPermission){
            val hasBackgroundLocationPermission
                    = ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (hasBackgroundLocationPermission){
                getTrack()
                checkLocationEnabled()
            } else {
                pLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                )
            }
        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun checkPermissionBefore10() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            getTrack()
            checkLocationEnabled()
        } else {
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun checkLocationEnabled(){
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled){
            showToast("Геолокация не включена")
            DialogManager.showLocEnableDialog(
                activity as AppCompatActivity,
                object : DialogManager.Listener{
                    override fun onClick() {
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
        registerPermissions()
        setOnClicks()
        checkServiceState()
        registerLocReceiver()
        locationUpdate()

        switch = binding.switch1

        val v = SharedPref.Companion.getValue(requireActivity())

        if (v != 0) {
            switch.isChecked = true
        } else {
            switch.isChecked = false
        }

        switchMain()
    }

    private fun setOnClicks() = with(binding){
        val listener = onClicks()
        fStartStop.setOnClickListener(listener)
        fCenter.setOnClickListener(listener)
    }

    private fun onClicks(): OnClickListener {
        return OnClickListener {
            when(it.id) {
                R.id.fStartStop -> startStopService()
                R.id.fCenter -> CoroutineScope(Dispatchers.IO).launch {
                    centerLocation()
                }
            }
        }
    }

    private suspend fun centerLocation() = withContext(Dispatchers.Main){
        val mLocProvider = GpsMyLocationProvider(activity)
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, binding.map)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation()
        binding.map.overlays.add(mLocOverlay)
    }

    @SuppressLint("DefaultLocale")
    private fun locationUpdate() = with(binding){
        model.locationUpdates.observe(viewLifecycleOwner){
            val distance = "${String.format("%.3f", it.distance / 1000.0f)} км"
            val kmPk = "${it.kmDistance} км ${it.pkDistance} пк"
            val speed = "${String.format("%.0f", 3.6f * it.speed)} км/h"
            tvDistance.text = distance
            tvKmPk.text = kmPk
            tvSpeed.text = speed
            CoroutineScope(Dispatchers.IO).launch {
                updatePolyline(it.geoPointsTrack)
            }
            val tvOgr15text = it.tvOgr15
            val tvOgr25text = it.tvOgr25
            val tvOgr40text = it.tvOgr40
            val tvOgr50text = it.tvOgr50
            val tvOgr55text = it.tvOgr55
            val tvOgr60text = it.tvOgr60
            val tvOgr65text = it.tvOgr65
            val tvOgr70text = it.tvOgr70
            val tvOgr75text = it.tvOgr75
            val tvKmPk15text = it.tvKmPk15
            val tvKmPk25text = it.tvKmPk25
            val tvKmPk40text = it.tvKmPk40
            val tvKmPk50text = it.tvKmPk50
            val tvKmPk55text = it.tvKmPk55
            val tvKmPk60text = it.tvKmPk60
            val tvKmPk65text = it.tvKmPk65
            val tvKmPk70text = it.tvKmPk70
            val tvKmPk75text = it.tvKmPk75
            tvOgr15.text = tvOgr15text
            tvOgr25.text = tvOgr25text
            tvOgr40.text = tvOgr40text
            tvOgr50.text = tvOgr50text
            tvOgr55.text = tvOgr55text
            tvOgr60.text = tvOgr60text
            tvOgr65.text = tvOgr65text
            tvOgr70.text = tvOgr70text
            tvOgr75.text = tvOgr75text
            tvKmPk15.text = tvKmPk15text
            tvKmPk25.text = tvKmPk25text
            tvKmPk40.text = tvKmPk40text
            tvKmPk50.text = tvKmPk50text
            tvKmPk55.text = tvKmPk55text
            tvKmPk60.text = tvKmPk60text
            tvKmPk65.text = tvKmPk65text
            tvKmPk70.text = tvKmPk70text
            tvKmPk75.text = tvKmPk75text
            locationModel = it
        }
    }

    @SuppressLint("ImplicitSamInstance")
    private fun startStopService() {
        if (!isServiceRunning && !MyLocationService.isRunning){
            startLocService()

        } else {

            activity?.stopService(Intent(activity, MyLocationService::class.java))
            binding.fStartStop.setImageResource(R.drawable.is_play)

            switch.isGone = switch.isVisible
            binding.map.isVisible = binding.map.isGone
            binding.fCenter.isVisible = binding.fCenter.isGone
            binding.tvDistance.isVisible = binding.tvDistance.isGone
            binding.tvSpeed.isVisible = binding.tvSpeed.isGone
            binding.tvKmPk.isVisible = binding.tvKmPk.isGone
            binding.tvOgr15.text = ""
            binding.tvOgr25.text = ""
            binding.tvOgr40.text = ""
            binding.tvOgr50.text = ""
            binding.tvOgr55.text = ""
            binding.tvOgr60.text = ""
            binding.tvOgr65.text = ""
            binding.tvOgr70.text = ""
            binding.tvOgr75.text = ""
            binding.tvKmPk15.text = ""
            binding.tvKmPk25.text = ""
            binding.tvKmPk40.text = ""
            binding.tvKmPk50.text = ""
            binding.tvKmPk55.text = ""
            binding.tvKmPk60.text = ""
            binding.tvKmPk65.text = ""
            binding.tvKmPk70.text = ""
            binding.tvKmPk75.text = ""

        }
        isServiceRunning = !isServiceRunning
    }

    private fun checkServiceState(){
        isServiceRunning = MyLocationService.isRunning
        switch = binding.switch1
        if (isServiceRunning){
            binding.fStartStop.setImageResource(R.drawable.is_stop)

            switch.isVisible = switch.isGone
            binding.map.isGone = binding.map.isVisible
            binding.fCenter.isGone = binding.fCenter.isVisible
            binding.tvDistance.isGone = binding.tvDistance.isVisible
            binding.tvSpeed.isGone = binding.tvSpeed.isVisible
            binding.tvKmPk.isGone = binding.tvKmPk.isVisible
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun startLocService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            activity?.startForegroundService(Intent(activity, MyLocationService::class.java))
        }else{
            activity?.startService(Intent(activity, MyLocationService::class.java))
        }
        binding.fStartStop.setImageResource(R.drawable.is_stop)
        //ggg

        switch.isVisible = switch.isGone
        binding.fCenter.isGone = binding.fCenter.isVisible
        binding.tvDistance.isGone = binding.tvDistance.isVisible
        binding.tvSpeed.isGone = binding.tvSpeed.isVisible
        binding.tvKmPk.isGone = binding.tvKmPk.isVisible
    }

    override fun onResume() {
        super.onResume()
        checkLocPermission()

        binding.tvOgr15.text = ""
        binding.tvOgr25.text = ""
        binding.tvOgr40.text = ""
        binding.tvOgr50.text = ""
        binding.tvOgr55.text = ""
        binding.tvOgr60.text = ""
        binding.tvOgr65.text = ""
        binding.tvOgr70.text = ""
        binding.tvOgr75.text = ""
        binding.tvKmPk15.text = ""
        binding.tvKmPk25.text = ""
        binding.tvKmPk40.text = ""
        binding.tvKmPk50.text = ""
        binding.tvKmPk55.text = ""
        binding.tvKmPk60.text = ""
        binding.tvKmPk65.text = ""
        binding.tvKmPk70.text = ""
        binding.tvKmPk75.text = ""

    }

    override fun onPause() {
        super.onPause()

        switch = binding.switch1

        val v = SharedPref.Companion.getValue(requireActivity())

        if (v != 0) {
            switch.isChecked = true
        } else {
            switch.isChecked = false
        }
    }

    private fun switchMain() {
        switch = binding.switch1

        switch.setOnClickListener {
            if (switch.isChecked) {
                SharedPref.Companion.setValue(requireActivity(), 1)
            } else {
                SharedPref.Companion.setValue(requireActivity(), 0)
            }
        }
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MyLocationService.LOC_MODEL_INTENT){
                val locModel = intent.getSerializableExtra(MyLocationService.LOC_MODEL_INTENT) as LocationModel
                model.locationUpdates.value = locModel
            }
        }
    }

    private fun registerLocReceiver(){
        val locFilter = IntentFilter(MyLocationService.LOC_MODEL_INTENT)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .registerReceiver(receiver, locFilter)
    }

    private fun getTrack() = with(binding) {

        map.controller.setZoom(13.0)
        val mLocProvider = GpsMyLocationProvider(activity)
        mLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation()
        mLocOverlay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(polyline)
            map.overlays.add(mLocOverlay)
        }

        model.currentTrack.observe(viewLifecycleOwner){
            val polyline = getPolyline(it.geoPoints)
//            map.overlays.add(polyline)
//            goToStartPosition(polyline.actualPoints[0])
//            centerLocation()
        }
    }

    private fun goToStartPosition(startPosition: GeoPoint){
        binding.map.controller.zoomTo(13.0)
        binding.map.controller.animateTo(startPosition)
    }

    private fun getPolyline(geoPoints: String): Polyline {

        val list = geoPoints.split("/")
        distanceArray.clear()
        list.forEach {
            if (it.isEmpty()) return@forEach
            val points = it.split(",")
            polyline1.addPoint(GeoPoint(points[0].toDouble(), points[1].toDouble()))
            distanceArray.add(points[2].toFloat())

            val fragmentLatLongKmToService = FragmentLatLongKmToServiceModel(
                geoPoints,

                )
            sendFragmentLatLongKmToService(fragmentLatLongKmToService)
        }
        return polyline1
    }

    private fun sendFragmentLatLongKmToService(fragmentLatLongKmToService: FragmentLatLongKmToServiceModel){
        val intentLatLongKmToService = Intent(FRAGMENT_LAT_LONG_KM_TO_SERVICE)
        intentLatLongKmToService.putExtra(FRAGMENT_LAT_LONG_KM_TO_SERVICE, fragmentLatLongKmToService)
        context?.let { LocalBroadcastManager.getInstance(it).sendBroadcast(intentLatLongKmToService) }
    }

    private suspend fun fillPolyline(list: ArrayList<GeoPoint>) = withContext(Dispatchers.IO){
        list.forEach {
            polyline.addPoint(it)
        }
    }

    private suspend fun updatePolyline(list: ArrayList<GeoPoint>) = withContext(Dispatchers.IO){
        if (list.size > 1 && firstStart){
            CoroutineScope(Dispatchers.IO).launch {
                fillPolyline(list)
            }
            firstStart = false
        }
    }

    override fun onDetach() {
        super.onDetach()
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .unregisterReceiver(receiver)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()

        const val FRAGMENT_LAT_LONG_KM_TO_SERVICE = "fragment_lat_long_km_to_service"
    }
}
