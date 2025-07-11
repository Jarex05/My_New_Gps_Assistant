package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.pantograph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentLoweringNechetBinding

class LoweringNechetFragment : Fragment() {

    private lateinit var binding: FragmentLoweringNechetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoweringNechetBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoweringNechetFragment()
    }
}