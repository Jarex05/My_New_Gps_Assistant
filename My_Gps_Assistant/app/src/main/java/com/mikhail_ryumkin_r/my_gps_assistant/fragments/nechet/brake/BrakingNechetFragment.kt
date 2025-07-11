package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.brake

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentBrakingNechetBinding

class BrakingNechetFragment : Fragment() {

    private lateinit var binding: FragmentBrakingNechetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentBrakingNechetBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = BrakingNechetFragment()
    }
}