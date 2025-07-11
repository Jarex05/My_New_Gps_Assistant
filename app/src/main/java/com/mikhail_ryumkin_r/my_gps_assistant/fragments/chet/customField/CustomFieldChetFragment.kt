package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.customField

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentCustomFieldChetBinding
import java.util.Locale

@Suppress("DEPRECATION")
class CustomFieldChetFragment : Fragment() {

//    lateinit var textToSpeech: TextToSpeech

    private lateinit var binding: FragmentCustomFieldChetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCustomFieldChetBinding.inflate(inflater, container, false)
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        var b1 = binding.btnSpeak
//        var editText1 = binding.editText
//
//        b1.setOnClickListener {
//            textToSpeech = TextToSpeech(requireActivity(), TextToSpeech.OnInitListener {
//                if (it == TextToSpeech.SUCCESS) {
//                    val local = Locale("us")
//                    textToSpeech.language = local
//                    textToSpeech.setSpeechRate(1.0f)
//                    textToSpeech.speak(editText1.text.toString(), TextToSpeech.QUEUE_ADD, null)
//                }
//            })
//        }
//    }

    companion object {
        @JvmStatic
        fun newInstance() = CustomFieldChetFragment()
    }
}