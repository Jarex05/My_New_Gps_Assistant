package com.mikhail_ryumkin_r.my_gps_assistant.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlin.toString

class SharedPref {
    companion object {
        @SuppressLint("UseKtx")
        fun setValue(context: Context, value: Int) {
            val sharedPref: SharedPreferences = context.getSharedPreferences("switch", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putInt("saveText", value)
            editor.apply()
        }

        fun getValue(context: Context): Int {
            val shredPref: SharedPreferences = context.getSharedPreferences("switch", Context.MODE_PRIVATE)
            return shredPref.getInt("saveText", 0)
        }

        @SuppressLint("UseKtx")
        fun setValueRecording(context: Context, value: Int) {
            val sharedPrefRecording: SharedPreferences = context.getSharedPreferences("switchRecording", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPrefRecording.edit()
            editor.putInt("saveTextRecording", value)
            editor.apply()
        }

        fun getValueRecording(context: Context): Int {
            val shredPrefRecording: SharedPreferences = context.getSharedPreferences("switchRecording", Context.MODE_PRIVATE)
            return shredPrefRecording.getInt("saveTextRecording", 0)
        }

        @SuppressLint("UseKtx")
        fun setValueKmAddd(context: Context, value: String) {
            val sharedPrefRecording: SharedPreferences = context.getSharedPreferences("kmAddd", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPrefRecording.edit()
            editor.putString("saveTextKmAddd", value).toString()
            editor.apply()
        }

        fun getValueKmAddd(context: Context): String {
            val shredPrefRecording: SharedPreferences = context.getSharedPreferences("kmAddd", Context.MODE_PRIVATE)
            return shredPrefRecording.getString("saveTextKmAddd", 0.toString()).toString()
        }

        @SuppressLint("UseKtx")
        fun setValueKmUpdate(context: Context, value: Int) {
            val sharedPrefRecording: SharedPreferences = context.getSharedPreferences("switchUpdate", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPrefRecording.edit()
            editor.putInt("saveTextUpdate", value)
            editor.apply()
        }

        fun getValueKmUpdate(context: Context): Int {
            val shredPrefRecording: SharedPreferences = context.getSharedPreferences("switchUpdate", Context.MODE_PRIVATE)
            return shredPrefRecording.getInt("saveTextUpdate", 0)
        }
    }
}