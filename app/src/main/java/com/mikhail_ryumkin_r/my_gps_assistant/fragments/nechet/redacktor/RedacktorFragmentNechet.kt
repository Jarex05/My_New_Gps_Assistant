package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.redacktor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.RedacktorNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.redacktor.RedacktorFragmentChet

class RedacktorFragmentNechet : Fragment() {
    private lateinit var binding: RedacktorNechetBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = RedacktorNechetBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = RedacktorFragmentNechet()
    }
}