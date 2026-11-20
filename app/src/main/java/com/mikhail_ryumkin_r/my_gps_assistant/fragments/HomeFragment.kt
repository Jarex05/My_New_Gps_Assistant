package com.mikhail_ryumkin_r.my_gps_assistant.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Switch
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.getValue
import kotlin.text.toDouble
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {
    private var polyline = Polyline()
    private var polyline1 = Polyline()
    private var isServiceRunning = false
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private var locationModel: LocationModel? = null
    private val distanceArray = ArrayList<Float>()
    private val listOgrFakt: MutableList<ListOgrFakt> = mutableListOf()
//    val listOgrFakt = mutableListOf<Int>()
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

    data class ListOgrFakt(val nachalo: Int,
                           val piketNachalo: Int,
                           val finish: Int,
                           val piketFinish: Int,
                           val speed: Int,){
        override fun toString(): String {
            return super.toString()
        }
    }

    private fun settingsOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private val backgroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showToast("Фоновое разрешение успешно получено!")
            getTrack()
//            checkLocationEnabled()
        } else {
            showToast("Фоновый доступ отклонен. Приложение не сможет работать в режиме сна")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClicks()
        checkServiceState()
        registerLocReceiver()
        locationUpdate()

        switch = binding.switch1

        val v = SharedPref.getValue(requireActivity())

        if (v != 0) {
            switch.isChecked = true
        } else {
            switch.isChecked = false
        }

        switchMain()
    }

    private fun checkAndRequestBackgroundPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            checkPermissionBefore10()
            return
        }

        val backgroundGranted = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (backgroundGranted) {
            getTrack()
        } else {
            showBackgroundRationaleDialog()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showBackgroundRationaleDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle("Требуется фоновый доступ")
            .setMessage("Этому приложению необходим доступ к геолокации " +
                    "в фоновом режиме для корректной работы приложения при выключенном экране телефона.\n\n" +
                    "На следующем экране выберите пункт 'Разрешить в любом режиме'.")
            .setPositiveButton("В настройки") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireActivity().packageName, null)
                    }
                    startActivity(intent)
                    showToast("Пожалуйста, выберите 'Разрешить в любом режиме'")
                } else {
                    backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
                showToast("Фоновый режим не активирован")
            }
            .create()
            .show()
    }

    private fun checkPermissionBefore10() {

        val fineLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocation) {
            getTrack()
        } else {
            backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
            checkAndRequestBackgroundPermission()
            showToast("Геолокация включена")
        }
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

    @SuppressLint("DefaultLocale", "UseKtx")
    private fun locationUpdate() = with(binding){
        model.locationUpdates.observe(viewLifecycleOwner){
            // Если нажата кнопка СТОП — игнорируем любые запоздалые сообщения от сервиса
            if (!isServiceRunning) return@observe
            val distance = "${String.format("%.3f", it.distance / 1000.0f)} км"
            val kmPk = "${it.kmDistance} км ${it.pkDistance} пк"
            val speed = "${String.format("%.0f", 3.6f * it.speed)} км/h"
            tvDistance.text = distance
            tvKmPk.text = kmPk
            tvSpeed.text = speed
            updatePolyline(it.geoPointsTrack)

            listOgrFakt.clear()

            // 1. Создаем конструктор красивого текста
            val builder = SpannableStringBuilder()

            if (it.listOgrFakt.isEmpty()) {
                val emptyText = "Ограничений нет"
                builder.append(emptyText)
                builder.setSpan(
                    ForegroundColorSpan(Color.GRAY),
                    0,
                    emptyText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                for ((index, item) in it.listOgrFakt.withIndex()) {
                    // Добавляем данные в локальный список
                    listOgrFakt.add(
                        ListOgrFakt(
                            item.nachalo,
                            item.piketNachalo,
                            item.finish,
                            item.piketFinish,
                            item.speed
                        )
                    )

                    // 1. СКОРОСТЬ (в самое начало, красная, крупная и жирная)
                    val speedStart = builder.length
                    val speedText = "${item.speed}  " // Пробелы для отступа от координат
                    builder.append(speedText)

                    builder.setSpan(
                        ForegroundColorSpan(Color.parseColor("#D32F2F")), // Насыщенный красный цвет
                        speedStart,
                        builder.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    builder.setSpan(
                        StyleSpan(Typeface.BOLD), // Жирный шрифт
                        speedStart,
                        builder.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    builder.setSpan(
                        RelativeSizeSpan(1.50f), // Увеличиваем размер на 25% (крупнее остального текста)
                        speedStart,
                        builder.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    // 2. НАЧАЛО — КОНЕЦ (чуть меньше скорости, но тоже немного жирные)
                    val coordsStart = builder.length
                    val coordsText = "${item.nachalo} км ${item.piketNachalo} пк — ${item.finish} км ${item.piketFinish} пк"
                    builder.append(coordsText)

                    builder.setSpan(
                        StyleSpan(Typeface.BOLD), // Жирное выделение для координат
                        coordsStart,
                        builder.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    builder.setSpan(
                        RelativeSizeSpan(1.30f), // Чуть больше обычного текста (1.05), но меньше скорости (1.25)
                        coordsStart,
                        builder.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    // 3. СБРОС НА НОВУЮ СТРОКУ (если это не последний элемент списка)
                    if (index < it.listOgrFakt.size - 1) {
                        builder.append("\n\n")
                    }
                }
            }

            // Выводим готовый результат в текстовое поле
            tvKmPkList.text = builder
            locationModel = it
        }
    }

    private fun resetUiOnStop() = with(binding) {
        listOgrFakt.clear()
        tvKmPkList.text = ""

        // 3. Явно возвращаем видимость переключателю (радиокнопке/свитчу) и скрываем карту/скорость
        switch1.isVisible = true
        map.isVisible = false
        fCenter.isVisible = false
        tvDistance.isVisible = false
        tvSpeed.isVisible = false
        tvKmPk.isVisible = false
    }

    @SuppressLint("ImplicitSamInstance")
    private fun startStopService() {
        if (!isServiceRunning && !MyLocationService.isRunning){
            isServiceRunning = true // Сначала ставим флаг
            startLocService()

        } else {
            isServiceRunning = false // Сначала сбрасываем флаг, чтобы заблокировать запоздалые данные из сервиса!

            activity?.stopService(Intent(activity, MyLocationService::class.java))
            binding.fStartStop.setImageResource(R.drawable.is_play)

            resetUiOnStop()

        }
    }

    private fun checkServiceState() = with(binding) {
        isServiceRunning = MyLocationService.isRunning
        switch = switch1
        if (isServiceRunning){
            binding.fStartStop.setImageResource(R.drawable.is_stop)

            // Явно скрываем переключатель и показываем рабочие поля
            switch1.isVisible = false
            map.isVisible = true
            fCenter.isVisible = true
            tvDistance.isVisible = true
            tvSpeed.isVisible = true
            tvKmPk.isVisible = true
        } else {
            // Если сервис не запущен при открытии фрагмента — приводим экран в исходное состояние
            fStartStop.setImageResource(R.drawable.is_play)
            resetUiOnStop()
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

        switch.isVisible = switch.isGone
        binding.fCenter.isGone = binding.fCenter.isVisible
        binding.tvDistance.isGone = binding.tvDistance.isVisible
        binding.tvSpeed.isGone = binding.tvSpeed.isVisible
        binding.tvKmPk.isGone = binding.tvKmPk.isVisible
    }

    override fun onResume() {
        super.onResume()
        checkLocationEnabled()
    }

    override fun onPause() {
        super.onPause()

        val fineLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocation || coarseLocation) {
            checkAndRequestBackgroundPermission()
        } else {
            return
        }

        Log.d("MyLog150", "onPause")
        mLocOverlay.disableMyLocation()

        switch = binding.switch1

        val v = SharedPref.getValue(requireActivity())

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
                SharedPref.setValue(requireActivity(), 1)
            } else {
                SharedPref.setValue(requireActivity(), 0)
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

    private fun fillPolyline(list: ArrayList<GeoPoint>) {
        list.forEach {
            polyline.addPoint(it)
        }
    }

    private fun updatePolyline(list: ArrayList<GeoPoint>) {
        if (list.size > 1 && firstStart){
            fillPolyline(list)
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
