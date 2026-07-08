package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.pantograph.update

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentUpdatePantographNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.pantograph.MyDbManagerPantograph
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.pantograph.update.UpdatePantographFragmentChet.ListKmAndPk
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdatePantographFragmentNechet : Fragment() {
    private lateinit var binding: FragmentUpdatePantographNechetBinding
    private val listKmPk = ArrayList<ListKmAndPk>()
    private val args by navArgs<UpdatePantographFragmentNechetArgs>()
    private lateinit var myDbManagerPantograph: MyDbManagerPantograph

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUpdatePantographNechetBinding.inflate(inflater, container, false)

        binding.edUpdateStartPantographNechet.setText(args.itemPantographNechet.startNechet.toString())
        binding.edUpdatePkStartPantographNechet.setText(args.itemPantographNechet.picketStartNechet.toString())

        binding.fbUpdateCancelPantographNechet.setOnClickListener {
            findNavController().navigate(R.id.action_updatePantographFragmentNechet_to_listPantographFragmentNechet)
        }

        binding.fbUpdateSavePantographNechet.setOnClickListener {
            updateNechet()
        }

        CoroutineScope(Dispatchers.Main).launch {

            for (a in myDbManagerPantograph.readDbDataPantographNechet()) {
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

    private fun init(v: View) {
        myDbManagerPantograph = MyDbManagerPantograph(v.context)
    }

    override fun onResume() {
        super.onResume()
        myDbManagerPantograph.openDb()
    }

    private fun updateNechet() {
        val startKm = (binding.edUpdateStartPantographNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val startPk = (binding.edUpdatePkStartPantographNechet.text.ifEmpty{ 0 }.toString()).toInt()

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
                    myDbManagerPantograph.updateDbDataPantographNechet(startKm, startPk, args.itemPantographNechet.idNechet)
                    findNavController().navigate(R.id.action_updatePantographFragmentNechet_to_listPantographFragmentNechet)
                    showToast("Сохранено")
                } else if (!isSavedYes && isSavedNo) {
                    showToast("Совпадение пикетов на одном километре")
                } else if (isSavedYes && !isSavedNo) {
                    myDbManagerPantograph.updateDbDataPantographNechet(startKm, startPk, args.itemPantographNechet.idNechet)
                    findNavController().navigate(R.id.action_updatePantographFragmentNechet_to_listPantographFragmentNechet)
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
        fun newInstance() = UpdatePantographFragmentNechet()
    }
}