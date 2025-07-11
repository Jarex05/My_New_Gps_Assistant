package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.limitation.update

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentUpdateLimitationsNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.limitation.MyDbManagerLimitations
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlin.getValue
import kotlin.text.ifEmpty

class UpdateLimitationsFragmentNechet : Fragment() {
    private lateinit var binding: FragmentUpdateLimitationsNechetBinding
    private val args by navArgs<UpdateLimitationsFragmentNechetArgs>()
    private lateinit var myDbManagerLimitations: MyDbManagerLimitations
    private var switchCheck = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUpdateLimitationsNechetBinding.inflate(inflater, container, false)

        binding.edUpdateStartLimitationsNechet.setText(args.itemLimitationsNechet.startNechet.toString())
        binding.edUpdatePkStartLimitationsNechet.setText(args.itemLimitationsNechet.picketStartNechet.toString())
        binding.edUpdateFinishLimitationsNechet.setText(args.itemLimitationsNechet.finishNechet.toString())
        binding.edUpdatePkFinishLimitationsNechet.setText(args.itemLimitationsNechet.picketFinishNechet.toString())
        binding.edUpdateSpeedLimitationsNechet.setText(args.itemLimitationsNechet.speedNechet.toString())
        binding.editLimitationNechetUpdateSwitch.setText(args.itemLimitationsNechet.switchNechetAdd.toString())

        switchCheck = binding.editLimitationNechetUpdateSwitch.text.toString()
        Log.d("MyLog", "switchCheck: $switchCheck")

        if (switchCheck == "1") {
            binding.switchLimitationNechetUpdate.isChecked = true
        } else {
            binding.switchLimitationNechetUpdate.isChecked = false
        }

        binding.switchLimitationNechetUpdate.setOnClickListener {
            if (binding.switchLimitationNechetUpdate.isChecked) {
                binding.editLimitationNechetUpdateSwitch.setText("1").toString()
            } else {
                binding.editLimitationNechetUpdateSwitch.setText("0").toString()
            }
        }

        binding.fbUpdateCancelLimitationsNechet.setOnClickListener {
            findNavController().navigate(R.id.action_updateLimitationsFragmentNechet_to_listLimitationsFragmentNechet)
        }

        binding.fbUpdateSaveLimitationsNechet.setOnClickListener {
            updateNechet()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
    }

    private fun init(v: View) {
        myDbManagerLimitations = MyDbManagerLimitations(v.context)
    }

    override fun onResume() {
        super.onResume()
        myDbManagerLimitations.openDb()
    }

    private fun updateNechet() {
        val startKm = (binding.edUpdateStartLimitationsNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val startPk = (binding.edUpdatePkStartLimitationsNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val finishKm = (binding.edUpdateFinishLimitationsNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val finishPk = (binding.edUpdatePkFinishLimitationsNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val speed = (binding.edUpdateSpeedLimitationsNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val switch = (binding.editLimitationNechetUpdateSwitch.text.ifEmpty{ 0 }.toString()).toInt()

        if (startKm != 0 && startPk != 0 && finishKm != 0 && finishPk != 0 && speed != 0){
            if (startKm.toString() != "" && startPk.toString() != "" && finishKm.toString() != "" && finishPk.toString() != "" && speed.toString() != ""){
                if (startPk.toString() == "1" || startPk.toString() == "2" || startPk.toString() == "3" || startPk.toString() == "4" || startPk.toString() == "5" || startPk.toString() == "6" || startPk.toString() == "7" || startPk.toString() == "8" || startPk.toString() == "9" || startPk.toString() == "10"){
                    if (finishPk.toString() == "1" || finishPk.toString() == "2" || finishPk.toString() == "3" || finishPk.toString() == "4" || finishPk.toString() == "5" || finishPk.toString() == "6" || finishPk.toString() == "7" || finishPk.toString() == "8" || finishPk.toString() == "9" || finishPk.toString() == "10"){
                        if (startKm > finishKm){
                            myDbManagerLimitations.updateDbDataLimitationsNechet(startKm, startPk, finishKm, finishPk, speed, switch, args.itemLimitationsNechet.idNechet)
                            SharedPref.Companion.setValueNechetStart(requireActivity(), startKm)
                            findNavController().navigate(R.id.action_updateLimitationsFragmentNechet_to_listLimitationsFragmentNechet)
                            showToast("Сохранено!")
                        }
                        if (startKm == finishKm){
                            if (startPk > finishPk){
                                myDbManagerLimitations.updateDbDataLimitationsNechet(startKm, startPk, finishKm, finishPk, speed, switch, args.itemLimitationsNechet.idNechet)
                                SharedPref.Companion.setValueNechetStart(requireActivity(), startKm)
                                findNavController().navigate(R.id.action_updateLimitationsFragmentNechet_to_listLimitationsFragmentNechet)
                                showToast("Сохранено!")
                            }
                            if (startPk == finishPk){
                                myDbManagerLimitations.updateDbDataLimitationsNechet(startKm, startPk, finishKm, finishPk, speed, switch, args.itemLimitationsNechet.idNechet)
                                SharedPref.Companion.setValueNechetStart(requireActivity(), startKm)
                                findNavController().navigate(R.id.action_updateLimitationsFragmentNechet_to_listLimitationsFragmentNechet)
                                showToast("Сохранено!")
                            }
                            if (startPk < finishPk){
                                showToast("Некорректные данные!")
                            }
                        }
                        if (startKm < finishKm){
                            showToast("Некорректные данные!")
                        }
                    }
                    else{
                        showToast("Поле 'Пикет' должно содержать число не менее '1' и не более '10'")
                    }
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
        myDbManagerLimitations.closeDb()
    }

    companion object {
        @JvmStatic
        fun newInstance() = UpdateLimitationsFragmentNechet()
    }
}