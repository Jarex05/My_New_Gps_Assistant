package com.mikhail_ryumkin_r.my_gps_assistant.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentNeutralInsertNechetBinding

class NeutralInsertNechetFragment : Fragment() {

    private lateinit var binding: FragmentNeutralInsertNechetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNeutralInsertNechetBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = NeutralInsertNechetFragment()
    }
}