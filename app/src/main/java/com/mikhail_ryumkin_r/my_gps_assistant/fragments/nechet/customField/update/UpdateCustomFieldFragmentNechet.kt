package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.customField.update

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentUpdateCustomFieldNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.customField.MyDbManagerCustomField
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlin.getValue
import kotlin.text.ifEmpty

class UpdateCustomFieldFragmentNechet : Fragment() {
    private lateinit var binding: FragmentUpdateCustomFieldNechetBinding
    private val args by navArgs<UpdateCustomFieldFragmentNechetArgs>()
    private lateinit var myDbManagerCustomField: MyDbManagerCustomField

    private lateinit var spinner: Spinner
    private var selectedItem: String? = null
    private var selectedText = ""
    private var selectedId: Int = 0

    private var textSpinner = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentUpdateCustomFieldNechetBinding.inflate(inflater, container, false)

        binding.edUpdateStartCustomFieldNechet.setText(args.itemCustomFieldNechet.startNechetCustom.toString())
        binding.edUpdatePkStartCustomFieldNechet.setText(args.itemCustomFieldNechet.picketStartNechetCustom.toString())
        binding.editUpdateCustomFieldNechet.setText(args.itemCustomFieldNechet.fieldNechetCustom)

        binding.fbUpdateCancelCustomFieldNechet.setOnClickListener {
            findNavController().navigate(R.id.action_updateCustomFieldFragmentNechet_to_listCustomFieldFragmentNechet)
        }

        binding.fbUpdateSaveCustomFieldNechet.setOnClickListener {
            updateChet()
        }

        spinner = binding.spinnerUpdateNechet
        val data = mutableListOf<String?>("Нейтральная вставка", "Посадка бригады", "Высадка бригады", "Включите саут", "Выключите саут", "Впереди блокпост", "Правильность пути")
        val adapter = ArrayAdapter<String?>(requireActivity(), android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        selectedText = args.itemCustomFieldNechet.fieldNechetCustom
        for( (index, element) in data.withIndex()){
            if (element == selectedText) {
                selectedId = index
            }
        }
        spinner.setSelection(selectedId)


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedItem = parent.getItemAtPosition(position).toString()
                textSpinner = selectedItem.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Действия, если ничего не выбрано
            }
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
        val startKm = (binding.edUpdateStartCustomFieldNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val startPk = (binding.edUpdatePkStartCustomFieldNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val customField = textSpinner

        if (startKm != 0 && startPk != 0){
            if (startKm.toString() != "" && startPk.toString() != ""){
                if (startPk.toString() == "1" || startPk.toString() == "2" || startPk.toString() == "3" || startPk.toString() == "4" || startPk.toString() == "5" || startPk.toString() == "6" || startPk.toString() == "7" || startPk.toString() == "8" || startPk.toString() == "9" || startPk.toString() == "10"){
                    myDbManagerCustomField.updateDbDataCustomFieldNechet(startKm, startPk, customField, args.itemCustomFieldNechet.idNechetCustom)
                    findNavController().navigate(R.id.action_updateCustomFieldFragmentNechet_to_listCustomFieldFragmentNechet)
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
        fun newInstance() = UpdateCustomFieldFragmentNechet()
    }
}