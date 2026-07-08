package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.customField.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentAddCustomFieldNechetBinding

import com.mikhail_ryumkin_r.my_gps_assistant.db.customField.MyDbManagerCustomField
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.customField.add.AddCustomFieldFragmentChet.ListKmAndPk
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty

class AddCustomFieldFragmentNechet : Fragment() {

    private lateinit var myDbManagerCustomField: MyDbManagerCustomField
    private lateinit var binding: FragmentAddCustomFieldNechetBinding
    private val listKmPk = ArrayList<ListKmAndPk>()
    private lateinit var spinner: Spinner

    private var textSpinner = ""

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

        spinner = binding.spinnerAddNechet
        val data = mutableListOf<String?>("Нейтральная вставка", "Посадка бригады", "Высадка бригады", "Включите саут", "Выключите саут", "Впереди блокпост", "Правильность пути", "Переезд")
        val adapter = ArrayAdapter<String?>(requireActivity(), android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(adapter)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                textSpinner = selectedItem
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Действия, если ничего не выбрано
            }
        }

        CoroutineScope(Dispatchers.Main).launch {

            for (a in myDbManagerCustomField.readDbDataCustomFieldNechet()) {
                listKmPk.add(ListKmAndPk(a.startNechetCustom, a.picketStartNechetCustom))
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
    }

    data class ListKmAndPk(val km: Int,
                           val pk: Int){
        override fun toString(): String {
            return super.toString()
        }
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
        val customField = textSpinner

        if (startKm != 0 && startPk != 0){
            if (startKm.toString() != "" && startPk.toString() != ""){
                if (startKm.toString() != "" && startPk.toString() != "" && startPk.toString() == "1" || startPk.toString() == "2" || startPk.toString() == "3" || startPk.toString() == "4" || startPk.toString() == "5" || startPk.toString() == "6" || startPk.toString() == "7" || startPk.toString() == "8" || startPk.toString() == "9" || startPk.toString() == "10"){

                    var list: MutableList<Pair<Int, Int>>
                    var isSavedYes = false
                    var isSavedNo = false

                    for (a in listKmPk.indices) {
                        list = mutableListOf(Pair(listKmPk[a].km, listKmPk[a].pk))

                        val newListPairKmPk = Pair(startKm, startPk)

                        if (list.none { it == newListPairKmPk }) {
                            list.add(newListPairKmPk)
                            isSavedYes = true
                        } else {
                            isSavedNo = true
                        }
                    }

                    if (!isSavedYes && !isSavedNo) {
                        myDbManagerCustomField.insertToDbCustomFieldNechet(startKm, startPk, customField)
                        findNavController().navigate(R.id.action_addCustomFieldFragmentNechet_to_listCustomFieldFragmentNechet)
                        showToast("Сохранено")
                    } else if (!isSavedYes && isSavedNo) {
                        showToast("Совпадение пикетов на одном километре")
                    } else if (isSavedYes && !isSavedNo) {
                        myDbManagerCustomField.insertToDbCustomFieldNechet(startKm, startPk, customField)
                        findNavController().navigate(R.id.action_addCustomFieldFragmentNechet_to_listCustomFieldFragmentNechet)
                        showToast("Сохранено")
                    } else if (isSavedYes && isSavedNo) {
                        showToast("Совпадение пикетов на одном километре")
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
        myDbManagerCustomField.closeDb()
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddCustomFieldFragmentNechet()
    }
}