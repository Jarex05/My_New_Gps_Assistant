package com.mikhail_ryumkin_r.my_gps_assistant.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentSetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import com.mikhail_ryumkin_r.my_gps_assistant.utils.showToast
import kotlin.toString

class SetFragment : Fragment() {

    private lateinit var binding: FragmentSetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val b = SharedPref.Companion.getValueKmAddd(requireActivity())

        if (b != "") {
            binding.edKmSave.setText(b)
        } else {
            binding.edKmSave.setText("")
        }

        val v = SharedPref.Companion.getValueUsl(requireActivity())

        if (v != "") {
            binding.edUsl.setText(v)
        } else {
            binding.edUsl.setText("")
        }

        switchMain()
    }

    override fun onPause() {
        super.onPause()

        val b = SharedPref.Companion.getValueKmAddd(requireActivity())

        if (b != "") {
            binding.edKmSave.setText(b)
        } else {
            binding.edKmSave.setText("")
        }

        val v = SharedPref.Companion.getValueUsl(requireActivity())

        if (v != "") {
            binding.edUsl.setText(v)
        } else {
            binding.edUsl.setText("")
        }
    }

    private fun switchMain() {

        binding.bSaveKm.setOnClickListener {
            val a = binding.edKmSave.text.toString()
            if (a != "") {
                binding.edKmSave.setText(binding.edKmSave.text).toString()
                SharedPref.Companion.setValueKmAddd(requireActivity(), a)
                showToast("Сохранено!")
            } else {
                SharedPref.Companion.setValueKmAddd(requireActivity(), 0.toString())
            }
        }

        binding.bSaveUsl.setOnClickListener {
            val a = binding.edUsl.text.toString()
            if (a != "") {
                binding.edUsl.setText(binding.edUsl.text).toString()
                SharedPref.Companion.setValueUsl(requireActivity(), a)
                showToast("Сохранено!")
            } else {
                SharedPref.Companion.setValueUsl(requireActivity(), 0.toString())
            }
        }
    }

    companion object {
        fun newInstance() = SetFragment()
    }
}