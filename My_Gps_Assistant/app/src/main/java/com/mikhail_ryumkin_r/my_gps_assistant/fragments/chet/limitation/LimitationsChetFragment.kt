package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.limitation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentLimitationsChetBinding

class LimitationsChetFragment : Fragment() {

    private lateinit var binding: FragmentLimitationsChetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLimitationsChetBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = LimitationsChetFragment()
    }
}