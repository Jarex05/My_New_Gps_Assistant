package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.pantograph.add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentAddPantographNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.pantograph.MyDbManagerPantograph
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast

class AddPantographFragmentNechet : Fragment() {
    private lateinit var binding: FragmentAddPantographNechetBinding
    private lateinit var myDbManagerPantograph: MyDbManagerPantograph

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddPantographNechetBinding.inflate(inflater, container, false)

        binding.fbSavePantographNechet.setOnClickListener {
            onClickSave()
        }

        binding.fbCancelPantographNechet.setOnClickListener {
            findNavController().navigate(R.id.action_addPantographFragmentNechet_to_listPantographFragmentNechet)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
    }

    override fun onResume() {
        super.onResume()
        myDbManagerPantograph.openDb()
    }

    private fun init(v: View) {
        myDbManagerPantograph = MyDbManagerPantograph(v.context)
    }

    private fun onClickSave() {
        val startKm = (binding.edStartPantographNechet.text.ifEmpty{ 0 }.toString()).toInt()
        val startPk = (binding.edPkStartPantographNechet.text.ifEmpty{ 0 }.toString()).toInt()

        if (startKm != 0 && startPk != 0){
            if (startKm.toString() != ("") && startPk.toString() != ("") && startPk.toString() == ("1") || startPk.toString() == ("2") || startPk.toString() == ("3") || startPk.toString() == ("4") || startPk.toString() == ("5") || startPk.toString() == ("6") || startPk.toString() == ("7") || startPk.toString() == ("8") || startPk.toString() == ("9")|| startPk.toString() == ("10")){
                myDbManagerPantograph.insertToDbPantographNechet(startKm, startPk)
                findNavController().navigate(R.id.action_addPantographFragmentNechet_to_listPantographFragmentNechet)
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
        fun newInstance() = AddPantographFragmentNechet()
    }
}