package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.limitation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentLimitationsNechetBinding

class LimitationsNechetFragment : Fragment() {

    private lateinit var binding: FragmentLimitationsNechetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLimitationsNechetBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = LimitationsNechetFragment()
    }
}