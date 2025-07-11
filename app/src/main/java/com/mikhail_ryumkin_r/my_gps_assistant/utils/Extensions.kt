package com.mikhail_ryumkin_r.my_gps_assistant.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mikhail_ryumkin_r.my_gps_assistant.R

fun Fragment.openFragment(f: Fragment){
    (activity as AppCompatActivity).supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        .replace(R.id.frame_layout, f).commit()
}

fun AppCompatActivity.openFragment(f: Fragment){
    if (supportFragmentManager.fragments.isNotEmpty()){
        if (supportFragmentManager.fragments[0].javaClass == f.javaClass) return
    }
    supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        .replace(R.id.frame_layout, f).commit()
}

fun Fragment.showToast(s: String) {
    Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
}

fun Activity.showToast(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}

fun Fragment.checkPermission(p: String): Boolean{
    return when(PackageManager.PERMISSION_GRANTED){
        ContextCompat.checkSelfPermission(activity as AppCompatActivity, p) -> true
        else -> false
    }
}