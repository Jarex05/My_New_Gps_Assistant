package com.mikhail_ryumkin_r.my_gps_assistant

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.ActivityMainBinding
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.HomeFragment
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.SettingsFragment
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.RecordingARouteFragment
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.RoutesFragment
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.BrakingChetFragment
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.brake.BrakingNechetFragment
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.limitation.LimitationsChetFragment
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.limitation.LimitationsNechetFragment
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.pantograph.LoweringChetFragment
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.pantograph.LoweringNechetFragment
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.SetFragment
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.customField.CustomFieldChetFragment
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.customField.CustomFieldNechetFragment
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import com.mikhail_ryumkin_r.my_gps_assistant.utils.openFragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding

    private lateinit var fab: FloatingActionButton
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
//            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//        }

        bottomNavigationView = binding.bottomNavigation
        fab = binding.fab
        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationDrawer
        toolbar = binding.toolbar

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.nav_open, R.string.nav_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.frame_layout, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.bottom_home)
        }

        binding.navigationDrawer.setNavigationItemSelectedListener(this)

        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.bottom_home -> openFragment(HomeFragment.newInstance())
                R.id.bottom_route -> openFragment(RoutesFragment.newInstance())
            }
            true
        }

        fragmentManager = supportFragmentManager
        openFragment(HomeFragment())

        binding.fab.setOnClickListener {
            showBottomDialog()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.recording_a_route -> openFragment(RecordingARouteFragment.newInstance())
            R.id.settings -> openFragment(SettingsFragment())
            R.id.setFragment -> openFragment(SetFragment())
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("UseKtx")
    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottomsheetlayout)

        val v = SharedPref.Companion.getValue(applicationContext)

        if (v != 0) {
            //***************************************************************

            val limitationNechetLayout : TextView = dialog.findViewById(R.id.layoutLimitationNechet)
            val linearLayoutLimitationNechetLayout : LinearLayout = dialog.findViewById(R.id.linearLayoutLimitationNechet)
            linearLayoutLimitationNechetLayout.isGone = linearLayoutLimitationNechetLayout.isVisible

            limitationNechetLayout.setOnClickListener {
                dialog.dismiss()
                openFragment(LimitationsNechetFragment.newInstance())
            }

            //***************************************************************

            val loweringNechetLayout : TextView = dialog.findViewById(R.id.layoutLoweringNechet)
            val linearLayoutLoweringNechetLayout : LinearLayout = dialog.findViewById(R.id.linearLayoutLoweringNechet)
            linearLayoutLoweringNechetLayout.isGone = linearLayoutLoweringNechetLayout.isVisible

            loweringNechetLayout.setOnClickListener {
                dialog.dismiss()
                openFragment(LoweringNechetFragment.newInstance())
            }

            //***************************************************************

            val brakingNechetLayout : TextView = dialog.findViewById(R.id.layoutBrakingNechet)
            val linearLayoutBrakingNechetLayout : LinearLayout = dialog.findViewById(R.id.linearLayoutBrakingNechet)
            linearLayoutBrakingNechetLayout.isGone = linearLayoutBrakingNechetLayout.isVisible

            brakingNechetLayout.setOnClickListener {
                dialog.dismiss()
                openFragment(BrakingNechetFragment.newInstance())
            }

            //***************************************************************

//            var neutralInsertNechetLayout : TextView = dialog.findViewById(R.id.layoutNeutral_InsertNechet)
//            var linearLayoutNeutralInsertNechetLayout : LinearLayout = dialog.findViewById(R.id.linearLayoutNeutral_InsertNechet)
//            linearLayoutNeutralInsertNechetLayout.isGone = linearLayoutNeutralInsertNechetLayout.isVisible
//
//            neutralInsertNechetLayout.setOnClickListener {
//                dialog.dismiss()
//                openFragment(MainCustomNechetFragment.newInstance())
//            }

            //***************************************************************

            val customFieldNechetLayout : TextView = dialog.findViewById(R.id.layoutCustom_FieldNechet)
            val linearLayoutCustomFieldNechetLayout : LinearLayout = dialog.findViewById(R.id.linearLayoutCustom_FieldNechet)
            linearLayoutCustomFieldNechetLayout.isGone = linearLayoutCustomFieldNechetLayout.isVisible

            customFieldNechetLayout.setOnClickListener {
                dialog.dismiss()
                openFragment(CustomFieldNechetFragment.newInstance())
            }

            //***************************************************************
        } else {
            //***************************************************************

            val limitationChetLayout: TextView = dialog.findViewById(R.id.layoutLimitationChet)
            val linearLayoutLimitationChetLayout: LinearLayout =
                dialog.findViewById(R.id.linearLayoutLimitationChet)
            linearLayoutLimitationChetLayout.isGone = linearLayoutLimitationChetLayout.isVisible

            limitationChetLayout.setOnClickListener {
                dialog.dismiss()
                openFragment(LimitationsChetFragment.newInstance())
            }

            //***************************************************************

            val loweringChetLayout: TextView = dialog.findViewById(R.id.layoutLoweringChet)
            val linearLayoutLoweringChetLayout: LinearLayout =
                dialog.findViewById(R.id.linearLayoutLoweringChet)
            linearLayoutLoweringChetLayout.isGone = linearLayoutLoweringChetLayout.isVisible

            loweringChetLayout.setOnClickListener {
                dialog.dismiss()
                openFragment(LoweringChetFragment.newInstance())
            }

            //***************************************************************

            val brakingChetLayout: TextView = dialog.findViewById(R.id.layoutBrakingChet)
            val linearLayoutBrakingChetLayout: LinearLayout =
                dialog.findViewById(R.id.linearLayoutBrakingChet)
            linearLayoutBrakingChetLayout.isGone = linearLayoutBrakingChetLayout.isVisible

            brakingChetLayout.setOnClickListener {
                dialog.dismiss()
                openFragment(BrakingChetFragment.newInstance())
            }

            //***************************************************************

//            var neutralInsertChetLayout: TextView =
//                dialog.findViewById(R.id.layoutNeutral_InsertChet)
//            var linearLayoutNeutralInsertChetLayout: LinearLayout =
//                dialog.findViewById(R.id.linearLayoutNeutral_InsertChet)
//            linearLayoutNeutralInsertChetLayout.isGone =
//                linearLayoutNeutralInsertChetLayout.isVisible
//
//            neutralInsertChetLayout.setOnClickListener {
//                dialog.dismiss()
//                openFragment(MainCustomChetFragment.newInstance())
//            }

            //***************************************************************

            val customFieldChetLayout: TextView = dialog.findViewById(R.id.layoutCustom_FieldChet)
            val linearLayoutCustomFieldChetLayout: LinearLayout =
                dialog.findViewById(R.id.linearLayoutCustom_FieldChet)
            linearLayoutCustomFieldChetLayout.isGone = linearLayoutCustomFieldChetLayout.isVisible

            customFieldChetLayout.setOnClickListener {
                dialog.dismiss()
                openFragment(CustomFieldChetFragment.newInstance())
            }

            //***************************************************************
        }

            //***************************************************************

        val cancelButton : ImageView = dialog.findViewById(R.id.cancelButton)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        //***************************************************************

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)


    }
}