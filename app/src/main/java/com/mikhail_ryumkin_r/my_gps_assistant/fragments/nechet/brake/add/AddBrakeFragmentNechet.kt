package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.brake.add

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.navigation.fragment.findNavController
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentAddBrakeNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.MyDbManagerBrake
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.add.AddBrakeFragmentChet.ListKmAndPk
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty

class AddBrakeFragmentNechet : Fragment() {

    private lateinit var binding: FragmentAddBrakeNechetBinding

    private val listKmPk = ArrayList<ListKmAndPk>()
    private lateinit var myDbManagerBrake: MyDbManagerBrake

    private lateinit var spinner: Spinner

    private var textSpinner = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentAddBrakeNechetBinding.inflate(inflater, container, false)

        binding.fbSaveBrakeNechet.setOnClickListener {
            onClickSave()
        }

        binding.fbCancelBrakeNechet.setOnClickListener {
            findNavController().navigate(R.id.action_addBrakeFragmentNechet_to_listBrakeFragmentNechet)
        }

        spinner = binding.spinnerAddBrakeNechet
        val data = mutableListOf<String?>("Проба тормозов", "Оживление тормозов")
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

            for (a in myDbManagerBrake.readDbDataBrakeNechet()) {
                listKmPk.add(ListKmAndPk(a.startNechet, a.picketStartNechet))
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
        myDbManagerBrake.openDb()
    }

    private fun init(v: View) {
        myDbManagerBrake = MyDbManagerBrake(v.context)
    }

    private fun onClickSave() {

        val startKm = (binding.edStartBrakeNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val startPk = (binding.edPkStartBrakeNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val textSpinner = textSpinner

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

                    Log.d("MyLog", "Значение isSavedYes = $isSavedYes")
                    Log.d("MyLog", "Значение isSavedNo = $isSavedNo")

                    if (!isSavedYes && !isSavedNo) {
                        myDbManagerBrake.insertToDbBrakeNechet(startKm, startPk, textSpinner)
                        findNavController().navigate(R.id.action_addBrakeFragmentNechet_to_listBrakeFragmentNechet)
                        showToast("Сохранено")
                    } else if (!isSavedYes && isSavedNo) {
                        showToast("Совпадение пикетов на одном километре")
                    } else if (isSavedYes && !isSavedNo) {
                        myDbManagerBrake.insertToDbBrakeNechet(startKm, startPk, textSpinner)
                        findNavController().navigate(R.id.action_addBrakeFragmentNechet_to_listBrakeFragmentNechet)
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
        myDbManagerBrake.closeDb()
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddBrakeFragmentNechet()
    }
}