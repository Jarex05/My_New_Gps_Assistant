package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.customField.update

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentUpdateBrakeChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentUpdateCustomFieldChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.MyDbManagerBrake
import com.mikhail_ryumkin_r.my_gps_assistant.db.customField.MyDbManagerCustomField
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.update.UpdateBrakeFragmentChet
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.update.UpdateBrakeFragmentChetArgs
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlin.getValue
import kotlin.text.ifEmpty

class UpdateCustomFieldFragmentChet : Fragment() {
    private lateinit var binding: FragmentUpdateCustomFieldChetBinding
    private val args by navArgs<UpdateCustomFieldFragmentChetArgs>()
    private lateinit var myDbManagerCustomField: MyDbManagerCustomField

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentUpdateCustomFieldChetBinding.inflate(inflater, container, false)

        binding.edUpdateStartCustomFieldChet.setText(args.itemCustomFieldChet.startChetCustom.toString())
        binding.edUpdatePkStartCustomFieldChet.setText(args.itemCustomFieldChet.picketStartChetCustom.toString())
        binding.editUpdateCustomFieldChet.setText(args.itemCustomFieldChet.fieldChetCustom)

        binding.fbUpdateCancelCustomFieldChet.setOnClickListener {
            findNavController().navigate(R.id.action_updateCustomFieldFragmentChet_to_listCustomFieldFragmentChet)
        }

        binding.fbUpdateSaveCustomFieldChet.setOnClickListener {
            updateChet()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
    }

    private fun init(v: View) {
        myDbManagerCustomField = MyDbManagerCustomField(v.context)
    }

    override fun onResume() {
        super.onResume()
        myDbManagerCustomField.openDb()
    }

    private fun updateChet() {
        val startKm = (binding.edUpdateStartCustomFieldChet.text.ifEmpty{ 0 }.toString()).toInt()
        val startPk = (binding.edUpdatePkStartCustomFieldChet.text.ifEmpty{ 0 }.toString()).toInt()
        val switch = (binding.editUpdateCustomFieldChet.text.ifEmpty{ 0 }.toString())

        if (startKm != 0 && startPk != 0){
            if (startKm.toString() != "" && startPk.toString() != ""){
                if (startPk.toString() == "1" || startPk.toString() == "2" || startPk.toString() == "3" || startPk.toString() == "4" || startPk.toString() == "5" || startPk.toString() == "6" || startPk.toString() == "7" || startPk.toString() == "8" || startPk.toString() == "9" || startPk.toString() == "10"){
                    myDbManagerCustomField.updateDbDataCustomFieldChet(startKm, startPk, switch, args.itemCustomFieldChet.idChetCustom)
                    findNavController().navigate(R.id.action_updateCustomFieldFragmentChet_to_listCustomFieldFragmentChet)
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
        myDbManagerCustomField.closeDb()
    }

    companion object {
        @JvmStatic
        fun newInstance() = UpdateCustomFieldFragmentChet()
    }
}