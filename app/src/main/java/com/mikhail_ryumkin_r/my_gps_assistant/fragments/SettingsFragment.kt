package com.mikhail_ryumkin_r.my_gps_assistant.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mikhail_ryumkin_r.my_gps_assistant.R

@Suppress("DEPRECATION")
class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var colorPrefLimitation: Preference
    private lateinit var colorPrefLimitationTime: Preference

    private lateinit var colorPrefBrake: Preference
    private lateinit var colorPrefBrakeRefresh: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preference, rootKey)
        init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init(){
        colorPrefLimitation = findPreference("color_limitation_key")!!
        colorPrefLimitationTime = findPreference("color_limitation_time_key")!!

        colorPrefBrake = findPreference("color_brake_key")!!
        colorPrefBrakeRefresh = findPreference("color_brake_refresh_key")!!

        val changeListener = onChangeListener()

        colorPrefLimitation.onPreferenceChangeListener = changeListener
        colorPrefLimitationTime.onPreferenceChangeListener = changeListener

        colorPrefBrake.onPreferenceChangeListener = changeListener
        colorPrefBrakeRefresh.onPreferenceChangeListener = changeListener
        initPrefs()
    }

    @SuppressLint("UseKtx")
    private fun onChangeListener(): Preference.OnPreferenceChangeListener{
        return Preference.OnPreferenceChangeListener{
                pref, value ->
            when(pref.key){
                "color_limitation_key" -> pref.icon?.setTint(Color.parseColor(value.toString()))
                "color_limitation_time_key" -> pref.icon?.setTint(Color.parseColor(value.toString()))

                "color_brake_key" -> pref.icon?.setTint(Color.parseColor(value.toString()))
                "color_brake_refresh_key" -> pref.icon?.setTint(Color.parseColor(value.toString()))
            }
            true
        }
    }

    @SuppressLint("UseKtx")
    private fun initPrefs(){
        //*********************************************************************************************************

        val prefLimitation = colorPrefLimitation.preferenceManager.sharedPreferences
        val prefLimitationTime = colorPrefLimitationTime.preferenceManager.sharedPreferences

        val trackColorLimitation = prefLimitation?.getString("color_limitation_key", "#FF009EDA")
        val trackColorLimitationTime = prefLimitationTime?.getString("color_limitation_time_key", "#FF009EDA")
        colorPrefLimitation.icon?.setTint(Color.parseColor(trackColorLimitation))
        colorPrefLimitationTime.icon?.setTint(Color.parseColor(trackColorLimitationTime))

        //*********************************************************************************************************

        val prefBrake = colorPrefBrake.preferenceManager.sharedPreferences
        val prefBrakeRefresh = colorPrefBrakeRefresh.preferenceManager.sharedPreferences

        val trackColorBrake = prefBrake?.getString("color_brake_key", "#FF009EDA")
        val trackColorBrakeRefresh = prefBrakeRefresh?.getString("color_brake_refresh_key", "#FF009EDA")
        colorPrefBrake.icon?.setTint(Color.parseColor(trackColorBrake))
        colorPrefBrakeRefresh.icon?.setTint(Color.parseColor(trackColorBrakeRefresh))

        //*********************************************************************************************************
    }
}