package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.update

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentUpdateBrakeChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.MyDbManagerBrake
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlin.getValue

class UpdateBrakeFragmentChet : Fragment() {

    private lateinit var binding: FragmentUpdateBrakeChetBinding
    private val args by navArgs<UpdateBrakeFragmentChetArgs>()
    private lateinit var myDbManagerBrake: MyDbManagerBrake
    private var switchCheck = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentUpdateBrakeChetBinding.inflate(inflater, container, false)

        binding.edUpdateStartBrakeChet.setText(args.itemBrakeChet.startChet.toString())
        binding.edUpdatePkStartBrakeChet.setText(args.itemBrakeChet.picketStartChet.toString())
        binding.editBrakeChetUpdateSwitch.setText(args.itemBrakeChet.switchChetAdd.toString())

        switchCheck = binding.editBrakeChetUpdateSwitch.text.toString()
        Log.d("MyLog", "switchCheck: $switchCheck")

        if (switchCheck == "1") {
            binding.switchBrakeChetUpdate.isChecked = true
            SharedPref.Companion.setValueChangeBrakeChet(requireActivity(), 1)
        } else {
            binding.switchBrakeChetUpdate.isChecked = false
            SharedPref.Companion.setValueChangeBrakeChet(requireActivity(), 0)
        }

        binding.switchBrakeChetUpdate.setOnClickListener {
            if (binding.switchBrakeChetUpdate.isChecked) {
                binding.editBrakeChetUpdateSwitch.setText("1").toString()
                SharedPref.Companion.setValueChangeBrakeChet(requireActivity(), 1)
            } else {
                binding.editBrakeChetUpdateSwitch.setText("0").toString()
                SharedPref.Companion.setValueChangeBrakeChet(requireActivity(), 0)
            }
        }

        binding.fbUpdateCancelBrakeChet.setOnClickListener {
            findNavController().navigate(R.id.action_updateBrakeFragmentChet_to_listBrakeFragmentChet)
        }

        binding.fbUpdateSaveBrakeChet.setOnClickListener {
            updateChet()
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

    private fun updateChet() {
        val startKm = (binding.edUpdateStartBrakeChet.text.ifEmpty{ 0 }.toString()).toInt()
        val startPk = (binding.edUpdatePkStartBrakeChet.text.ifEmpty{ 0 }.toString()).toInt()
        val switch = (binding.editBrakeChetUpdateSwitch.text.ifEmpty{ 0 }.toString()).toInt()

        if (startKm != 0 && startPk != 0){
            if (startKm.toString() != "" && startPk.toString() != ""){
                if (startPk.toString() == "1" || startPk.toString() == "2" || startPk.toString() == "3" || startPk.toString() == "4" || startPk.toString() == "5" || startPk.toString() == "6" || startPk.toString() == "7" || startPk.toString() == "8" || startPk.toString() == "9" || startPk.toString() == "10"){
                    myDbManagerBrake.updateDbDataBrakeChet(startKm, startPk, switch, args.itemBrakeChet.idChet)
                    findNavController().navigate(R.id.action_updateBrakeFragmentChet_to_listBrakeFragmentChet)
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
        fun newInstance() = UpdateBrakeFragmentChet()
    }
}