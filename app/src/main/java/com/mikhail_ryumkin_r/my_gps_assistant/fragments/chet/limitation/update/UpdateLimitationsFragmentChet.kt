package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.limitation.update

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentUpdateLimitationsChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.limitation.MyDbManagerLimitations
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlin.getValue
import kotlin.text.ifEmpty

class UpdateLimitationsFragmentChet : Fragment() {
    private lateinit var binding: FragmentUpdateLimitationsChetBinding
    private val args by navArgs<UpdateLimitationsFragmentChetArgs>()
    private lateinit var myDbManagerLimitations: MyDbManagerLimitations
    private var switchCheck = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUpdateLimitationsChetBinding.inflate(inflater, container, false)

        binding.edUpdateStartLimitationsChet.setText(args.itemLimitationsChet.startChet.toString())
        binding.edUpdatePkStartLimitationsChet.setText(args.itemLimitationsChet.picketStartChet.toString())
        binding.edUpdateFinishLimitationsChet.setText(args.itemLimitationsChet.finishChet.toString())
        binding.edUpdatePkFinishLimitationsChet.setText(args.itemLimitationsChet.picketFinishChet.toString())
        binding.edUpdateSpeedLimitationsChet.setText(args.itemLimitationsChet.speedChet.toString())
        binding.editLimitationChetUpdateSwitch.setText(args.itemLimitationsChet.switchChetAdd.toString())

        switchCheck = binding.editLimitationChetUpdateSwitch.text.toString()
        Log.d("MyLog", "switchCheck: $switchCheck")

        if (switchCheck == "1") {
            binding.switchLimitationChetUpdate.isChecked = true
        } else {
            binding.switchLimitationChetUpdate.isChecked = false
        }

        binding.switchLimitationChetUpdate.setOnClickListener {
            if (binding.switchLimitationChetUpdate.isChecked) {
                binding.editLimitationChetUpdateSwitch.setText("1").toString()
            } else {
                binding.editLimitationChetUpdateSwitch.setText("0").toString()
            }
        }

        binding.fbUpdateCancelLimitationsChet.setOnClickListener {
            findNavController().navigate(R.id.action_updateLimitationsFragmentChet_to_listLimitationsFragmentChet)
        }

        binding.fbUpdateSaveLimitationsChet.setOnClickListener {
            updateChet()
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

    private fun updateChet() {
        val startKm = (binding.edUpdateStartLimitationsChet.text.ifEmpty{ 0 }.toString()).toInt()
        val startPk = (binding.edUpdatePkStartLimitationsChet.text.ifEmpty{ 0 }.toString()).toInt()
        val finishKm = (binding.edUpdateFinishLimitationsChet.text.ifEmpty{ 0 }.toString()).toInt()
        val finishPk = (binding.edUpdatePkFinishLimitationsChet.text.ifEmpty{ 0 }.toString()).toInt()
        val speed = (binding.edUpdateSpeedLimitationsChet.text.ifEmpty{ 0 }.toString()).toInt()
        val switch = (binding.editLimitationChetUpdateSwitch.text.ifEmpty{ 0 }.toString()).toInt()

        if (startKm != 0 && startPk != 0 && finishKm != 0 && finishPk != 0 && speed != 0){
            if (startKm.toString() != "" && startPk.toString() != "" && finishKm.toString() != "" && finishPk.toString() != "" && speed.toString() != ""){
                if (startPk.toString() == "1" || startPk.toString() == "2" || startPk.toString() == "3" || startPk.toString() == "4" || startPk.toString() == "5" || startPk.toString() == "6" || startPk.toString() == "7" || startPk.toString() == "8" || startPk.toString() == "9" || startPk.toString() == "10"){
                    if (finishPk.toString() == "1" || finishPk.toString() == "2" || finishPk.toString() == "3" || finishPk.toString() == "4" || finishPk.toString() == "5" || finishPk.toString() == "6" || finishPk.toString() == "7" || finishPk.toString() == "8" || finishPk.toString() == "9" || finishPk.toString() == "10"){
                        if (startKm < finishKm){
                            myDbManagerLimitations.updateDbDataLimitationsChet(startKm, startPk, finishKm, finishPk, speed, switch, args.itemLimitationsChet.idChet)
                            SharedPref.Companion.setValueChetStart(requireActivity(), startKm)
                            findNavController().navigate(R.id.action_updateLimitationsFragmentChet_to_listLimitationsFragmentChet)
                            showToast("Сохранено!")
                        }
                        if (startKm == finishKm){
                            if (startPk < finishPk){
                                myDbManagerLimitations.updateDbDataLimitationsChet(startKm, startPk, finishKm, finishPk, speed, switch, args.itemLimitationsChet.idChet)
                                SharedPref.Companion.setValueChetStart(requireActivity(), startKm)
                                findNavController().navigate(R.id.action_updateLimitationsFragmentChet_to_listLimitationsFragmentChet)
                                showToast("Сохранено!")
                            }
                            if (startPk == finishPk){
                                myDbManagerLimitations.updateDbDataLimitationsChet(startKm, startPk, finishKm, finishPk, speed, switch, args.itemLimitationsChet.idChet)
                                SharedPref.Companion.setValueChetStart(requireActivity(), startKm)
                                findNavController().navigate(R.id.action_updateLimitationsFragmentChet_to_listLimitationsFragmentChet)
                                showToast("Сохранено!")
                            }
                            if (startPk > finishPk){
                                showToast("Некорректные данные!")
                            }
                        }
                        if (startKm > finishKm){
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
        fun newInstance() = UpdateLimitationsFragmentChet()
    }
}