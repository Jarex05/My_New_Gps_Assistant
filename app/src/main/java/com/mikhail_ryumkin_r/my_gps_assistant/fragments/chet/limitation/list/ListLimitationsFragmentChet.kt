package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.limitation.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.mikhail_ryumkin_r.my_gps_assistant.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentListLimitationsChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.limitation.MyDbManagerLimitations
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ListLimitationsFragmentChet : Fragment() {
    private lateinit var binding: FragmentListLimitationsChetBinding
    private lateinit var myDbManagerLimitations: MyDbManagerLimitations
    private val adapterLimitationsChet = AdapterLimitationsChet(ArrayList(), this)

    private var toIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentListLimitationsChetBinding.inflate(inflater, container, false)

        binding.fbAddLimitationsChet.setOnClickListener {
            findNavController().navigate(R.id.action_listLimitationsFragmentChet_to_addLimitationsFragmentChet)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
    }

    override fun onResume() {
        super.onResume()
        myDbManagerLimitations.openDb()
        fillAdapter()
    }

    private fun init(v: View) = with(binding) {
        myDbManagerLimitations = MyDbManagerLimitations(v.context)
        rcLimitationsChet.layoutManager = LinearLayoutManager(requireContext())
        val swapHelper = getSwapMg()
        swapHelper.attachToRecyclerView(rcLimitationsChet)
        rcLimitationsChet.adapter = adapterLimitationsChet
    }

    private fun fillAdapter() {
        CoroutineScope(Dispatchers.Main).launch {
            adapterLimitationsChet.updateLimitationsChet(myDbManagerLimitations.readDbDataLimitationsChet())

            val numbers = mutableListOf<Int>()

            val v = SharedPref.Companion.getValueChetStart(requireActivity())

            for (a in myDbManagerLimitations.readDbDataLimitationsChet()) {
                numbers.add(a.startChet)
                numbers.sort()
            }

            var index1 = 0

            numbers.forEachIndexed { index, element ->
                index1 = element
                if (index1 == v) {
                    toIndex = index
                }
            }
            binding.rcLimitationsChet.scrollToPosition(toIndex)
            SharedPref.Companion.setValueChetStart(requireActivity(), 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myDbManagerLimitations.closeDb()
    }

    private fun getSwapMg() : ItemTouchHelper{
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapterLimitationsChet.removeItemLimitationsChet(viewHolder.adapterPosition, myDbManagerLimitations)
            }

        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = ListLimitationsFragmentChet()
    }
}