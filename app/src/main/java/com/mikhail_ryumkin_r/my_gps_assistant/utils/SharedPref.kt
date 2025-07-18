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
        fun setValueUsl(context: Context, value: String) {
            val sharedPrefRecording: SharedPreferences = context.getSharedPreferences("usl", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPrefRecording.edit()
            editor.putString("usl", value).toString()
            editor.apply()
        }

        fun getValueUsl(context: Context): String {
            val shredPrefRecording: SharedPreferences = context.getSharedPreferences("usl", Context.MODE_PRIVATE)
            return shredPrefRecording.getString("usl", 0.toString()).toString()
        }

        @SuppressLint("UseKtx")
        fun setValueChangeBrakeChet(context: Context, value: Int) {
            val sharedPrefRecording: SharedPreferences = context.getSharedPreferences("switchChangeBrakeChet", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPrefRecording.edit()
            editor.putInt("saveChangeBrakeChet", value)
            editor.apply()
        }

        fun getValueChangeBrakeChet(context: Context): Int {
            val shredPrefRecording: SharedPreferences = context.getSharedPreferences("switchChangeBrakeChet", Context.MODE_PRIVATE)
            return shredPrefRecording.getInt("saveChangeBrakeChet", 0)
        }

        @SuppressLint("UseKtx")
        fun setValueChangeBrakeNechet(context: Context, value: Int) {
            val sharedPrefRecording: SharedPreferences = context.getSharedPreferences("switchChangeBrakeNechet", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPrefRecording.edit()
            editor.putInt("saveChangeBrakeNechet", value)
            editor.apply()
        }

        fun getValueChangeBrakeNechet(context: Context): Int {
            val shredPrefRecording: SharedPreferences = context.getSharedPreferences("switchChangeBrakeNechet", Context.MODE_PRIVATE)
            return shredPrefRecording.getInt("saveChangeBrakeNechet", 0)
        }

        @SuppressLint("UseKtx")
        fun setValueChetStart(context: Context, value: Int) {
            val sharedPrefRecording: SharedPreferences = context.getSharedPreferences("startChet", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPrefRecording.edit()
            editor.putInt("startChet", value)
            editor.apply()
        }

        fun getValueChetStart(context: Context): Int {
            val shredPrefRecording: SharedPreferences = context.getSharedPreferences("startChet", Context.MODE_PRIVATE)
            return shredPrefRecording.getInt("startChet", 0)
        }

        @SuppressLint("UseKtx")
        fun setValueNechetStart(context: Context, value: Int) {
            val sharedPrefRecording: SharedPreferences = context.getSharedPreferences("startNechet", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPrefRecording.edit()
            editor.putInt("startNechet", value)
            editor.apply()
        }

        fun getValueNechetStart(context: Context): Int {
            val shredPrefRecording: SharedPreferences = context.getSharedPreferences("startNechet", Context.MODE_PRIVATE)
            return shredPrefRecording.getInt("startNechet", 0)
        }

        @SuppressLint("UseKtx")
        fun setValueNotification(context: Context, value: String) {
            val sharedPrefRecording: SharedPreferences = context.getSharedPreferences("notification", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPrefRecording.edit()
            editor.putString("notification", value).toString()
            editor.apply()
        }

        fun getValueNotification(context: Context): String {
            val shredPrefRecording: SharedPreferences = context.getSharedPreferences("notification", Context.MODE_PRIVATE)
            return shredPrefRecording.getString("notification", 0.toString()).toString()
        }
    }
}