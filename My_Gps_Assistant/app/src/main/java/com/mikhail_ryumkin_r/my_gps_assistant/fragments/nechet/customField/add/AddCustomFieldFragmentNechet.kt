package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.customField.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentAddCustomFieldNechetBinding

import com.mikhail_ryumkin_r.my_gps_assistant.db.customField.MyDbManagerCustomField
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlin.text.ifEmpty

class AddCustomFieldFragmentNechet : Fragment() {

    private lateinit var myDbManagerCustomField: MyDbManagerCustomField
    private lateinit var binding: FragmentAddCustomFieldNechetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAddCustomFieldNechetBinding.inflate(inflater, container, false)

        binding.fbSaveCustomFieldNechet.setOnClickListener {
            onClickSave()
        }

        binding.fbCancelCustomFieldNechet.setOnClickListener {
            findNavController().navigate(R.id.action_addCustomFieldFragmentNechet_to_listCustomFieldFragmentNechet)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
    }

    override fun onResume() {
        super.onResume()
        myDbManagerCustomField.openDb()
    }

    private fun init(v: View) {
        myDbManagerCustomField = MyDbManagerCustomField(v.context)
    }

    private fun onClickSave() {

        val startKm = (binding.edStartCustomFieldNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val startPk = (binding.edPkStartCustomFieldNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val customField = (binding.editAddCustomFieldNechet.text.ifEmpty{ 0 }.toString())

        if (startKm != 0 && startPk != 0){
            if (startKm.toString() != "" && startPk.toString() != ""){
                if (startKm.toString() != "" && startPk.toString() != "" && startPk.toString() == "1" || startPk.toString() == "2" || startPk.toString() == "3" || startPk.toString() == "4" || startPk.toString() == "5" || startPk.toString() == "6" || startPk.toString() == "7" || startPk.toString() == "8" || startPk.toString() == "9" || startPk.toString() == "10"){
                    myDbManagerCustomField.insertToDbCustomFieldNechet(startKm, startPk, customField)
                    findNavController().navigate(R.id.action_addCustomFieldFragmentNechet_to_listCustomFieldFragmentNechet)
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
        fun newInstance() = AddCustomFieldFragmentNechet()
    }
}