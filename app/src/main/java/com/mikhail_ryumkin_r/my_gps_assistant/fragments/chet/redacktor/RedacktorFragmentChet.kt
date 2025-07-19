package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.redacktor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.RedacktorChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.pantograph.LoweringChetFragment

class RedacktorFragmentChet : Fragment() {
    private lateinit var binding: RedacktorChetBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = RedacktorChetBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = RedacktorFragmentChet()
    }
}