package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.brake.update

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
import androidx.navigation.fragment.navArgs
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentUpdateBrakeNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.MyDbManagerBrake
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.add.AddBrakeFragmentChet.ListKmAndPk
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty

class UpdateBrakeFragmentNechet : Fragment() {
    private lateinit var binding: FragmentUpdateBrakeNechetBinding
    private val listKmPk = ArrayList<ListKmAndPk>()
    private val args by navArgs<UpdateBrakeFragmentNechetArgs>()
    private lateinit var myDbManagerBrake: MyDbManagerBrake

    private lateinit var spinner: Spinner

    private var selectedItem: String? = null
    private var selectedText = ""
    private var selectedId: Int = 0

    private var textSpinner = ""
    private var switchCheck = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUpdateBrakeNechetBinding.inflate(inflater, container, false)

        binding.edUpdateStartBrakeNechet.setText(args.itemBrakeNechet.startNechet.toString())
        binding.edUpdatePkStartBrakeNechet.setText(args.itemBrakeNechet.picketStartNechet.toString())
        binding.editBrakeNechetUpdate.setText(args.itemBrakeNechet.fieldNechetCustom)

        switchCheck = binding.editBrakeNechetUpdate.text.toString()
        Log.d("MyLog", "switchCheck: $switchCheck")

        binding.fbUpdateCancelBrakeNechet.setOnClickListener {
            findNavController().navigate(R.id.action_updateBrakeFragmentNechet_to_listBrakeFragmentNechet)
        }

        binding.fbUpdateSaveBrakeNechet.setOnClickListener {
            updateNechet()
        }

        spinner = binding.spinnerUpdateNechet
        val data = mutableListOf<String?>("Проба тормозов", "Оживление тормозов")
        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        selectedText = args.itemBrakeNechet.fieldNechetCustom
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

        CoroutineScope(Dispatchers.Main).launch {

            for (a in myDbManagerBrake.readDbDataBrakeNechet()) {
                listKmPk.add(ListKmAndPk(a.startNechet, a.picketStartNechet, a.fieldNechetCustom))
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
    }

    data class ListKmAndPk(val km: Int,
                           val pk: Int,
                           val field: String){
        override fun toString(): String {
            return super.toString()
        }
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
        val textSpinner = textSpinner

        if (startKm != 0 && startPk != 0){
            if (startKm.toString() != "" && startPk.toString() != ""){
                if (startPk.toString() == "1" || startPk.toString() == "2" || startPk.toString() == "3" || startPk.toString() == "4" || startPk.toString() == "5" || startPk.toString() == "6" || startPk.toString() == "7" || startPk.toString() == "8" || startPk.toString() == "9" || startPk.toString() == "10"){

                    var list: MutableList<Triple<Int, Int, String>>
                    var isSavedYes = false
                    var isSavedNo = false

                    for (a in listKmPk.indices) {
                        list = mutableListOf(Triple(listKmPk[a].km, listKmPk[a].pk, listKmPk[a].field))

                        val newListPairKmPk = Triple(startKm, startPk, textSpinner)

                        if (list.none { it == newListPairKmPk }) {
                            list.add(newListPairKmPk)
                            isSavedYes = true
                        } else {
                            isSavedNo = true
                        }
                    }

                    if (!isSavedYes && !isSavedNo) {
                        myDbManagerBrake.updateDbDataBrakeNechet(startKm, startPk, textSpinner, args.itemBrakeNechet.idNechet)
                        findNavController().navigate(R.id.action_updateBrakeFragmentNechet_to_listBrakeFragmentNechet)
                        showToast("Сохранено")
                    } else if (!isSavedYes && isSavedNo) {
                        showToast("Совпадение пикетов на одном километре")
                    } else if (isSavedYes && !isSavedNo) {
                        myDbManagerBrake.updateDbDataBrakeNechet(startKm, startPk, textSpinner, args.itemBrakeNechet.idNechet)
                        findNavController().navigate(R.id.action_updateBrakeFragmentNechet_to_listBrakeFragmentNechet)
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
        fun newInstance() = UpdateBrakeFragmentNechet()
    }
}