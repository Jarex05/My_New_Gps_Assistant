package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.brake.update

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentUpdateBrakeNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.MyDbManagerBrake
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlin.text.ifEmpty

class UpdateBrakeFragmentNechet : Fragment() {
    private lateinit var binding: FragmentUpdateBrakeNechetBinding
    private val args by navArgs<UpdateBrakeFragmentNechetArgs>()
    private lateinit var myDbManagerBrake: MyDbManagerBrake
    private var switchCheck = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUpdateBrakeNechetBinding.inflate(inflater, container, false)

        binding.edUpdateStartBrakeNechet.setText(args.itemBrakeNechet.startNechet.toString())
        binding.edUpdatePkStartBrakeNechet.setText(args.itemBrakeNechet.picketStartNechet.toString())
        binding.editBrakeNechetUpdateSwitch.setText(args.itemBrakeNechet.switchNechetAdd.toString())

        switchCheck = binding.editBrakeNechetUpdateSwitch.text.toString()
        Log.d("MyLog", "switchCheck: $switchCheck")

        if (switchCheck == "1") {
            binding.switchBrakeNechetUpdate.isChecked = true
        } else {
            binding.switchBrakeNechetUpdate.isChecked = false
        }

        binding.switchBrakeNechetUpdate.setOnClickListener {
            if (binding.switchBrakeNechetUpdate.isChecked) {
                binding.editBrakeNechetUpdateSwitch.setText("1").toString()
            } else {
                binding.editBrakeNechetUpdateSwitch.setText("0").toString()
            }
        }

        binding.fbUpdateCancelBrakeNechet.setOnClickListener {
            findNavController().navigate(R.id.action_updateBrakeFragmentNechet_to_listBrakeFragmentNechet)
        }

        binding.fbUpdateSaveBrakeNechet.setOnClickListener {
            updateNechet()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
    }

    private fun init(v: View) {
        myDbManagerBrake = MyDbManagerBrake(v.context)
    }

    override fun onResume() {
        super.onResume()
        myDbManagerBrake.openDb()
    }

    private fun updateNechet() {
        val startKm = (binding.edUpdateStartBrakeNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val startPk = (binding.edUpdatePkStartBrakeNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val switch = (binding.editBrakeNechetUpdateSwitch.text.ifEmpty{ 0 }.toString()).toInt()

        if (startKm != 0 && startPk != 0){
            if (startKm.toString() != "" && startPk.toString() != ""){
                if (startPk.toString() == "1" || startPk.toString() == "2" || startPk.toString() == "3" || startPk.toString() == "4" || startPk.toString() == "5" || startPk.toString() == "6" || startPk.toString() == "7" || startPk.toString() == "8" || startPk.toString() == "9" || startPk.toString() == "10"){
                    myDbManagerBrake.updateDbDataBrakeNechet(startKm, startPk, switch, args.itemBrakeNechet.idNechet)
                    findNavController().navigate(R.id.action_updateBrakeFragmentNechet_to_listBrakeFragmentNechet)
                }
                else{
                    showToast("Поле 'Пикет' должно содержать число не менее '1' и не более '10'")
                }
            }
        } else {
            showToast("Вы не ввели значения для сохранения!")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myDbManagerBrake.closeDb()
    }

    companion object {
        @JvmStatic
        fun newInstance() = UpdateBrakeFragmentNechet()
    }
}