package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.pantograph.add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentAddPantographChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.pantograph.MyDbManagerPantograph
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.add.AddBrakeFragmentChet.ListKmAndPk
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddPantographFragmentChet : Fragment() {
    private lateinit var binding: FragmentAddPantographChetBinding
    private val listKmPk = ArrayList<ListKmAndPk>()
    private lateinit var myDbManagerPantograph: MyDbManagerPantograph

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddPantographChetBinding.inflate(inflater, container, false)

        binding.fbSavePantographChet.setOnClickListener {
            onClickSave()
        }

        binding.fbCancelPantographChet.setOnClickListener {
            findNavController().navigate(R.id.action_addPantographFragmentChet_to_listPantographFragmentChet)
        }

        CoroutineScope(Dispatchers.Main).launch {

            for (a in myDbManagerPantograph.readDbDataPantographChet()) {
                listKmPk.add(ListKmAndPk(a.startChet, a.picketStartChet))
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
        myDbManagerPantograph.openDb()
    }

    private fun init(v: View) {
        myDbManagerPantograph = MyDbManagerPantograph(v.context)
    }

    private fun onClickSave() {
        val startKm = (binding.edStartPantographChet.text.ifEmpty{ 0 }.toString()).toInt()
        val startPk = (binding.edPkStartPantographChet.text.ifEmpty{ 0 }.toString()).toInt()

        if (startKm != 0 && startPk != 0){
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
                    myDbManagerPantograph.insertToDbPantographChet(startKm, startPk)
                    findNavController().navigate(R.id.action_addPantographFragmentChet_to_listPantographFragmentChet)
                    showToast("Сохранено")
                } else if (!isSavedYes && isSavedNo) {
                    showToast("Совпадение пикетов на одном километре")
                } else if (isSavedYes && !isSavedNo) {
                    myDbManagerPantograph.insertToDbPantographChet(startKm, startPk)
                    findNavController().navigate(R.id.action_addPantographFragmentChet_to_listPantographFragmentChet)
                    showToast("Сохранено")
                } else if (isSavedYes && isSavedNo) {
                    showToast("Совпадение пикетов на одном километре")
                }

            }
            else{
                showToast("Поле 'Пикет' должно содержать число не менее '1' и не более '10'")
            }
        } else {
            showToast("Вы не ввели значения для сохранения!")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myDbManagerPantograph.closeDb()
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddPantographFragmentChet()
    }
}