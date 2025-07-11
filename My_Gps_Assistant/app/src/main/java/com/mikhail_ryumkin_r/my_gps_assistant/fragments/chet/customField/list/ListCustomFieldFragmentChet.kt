package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.customField.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentListBrakeChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentListCustomFieldChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.MyDbManagerBrake
import com.mikhail_ryumkin_r.my_gps_assistant.db.customField.MyDbManagerCustomField
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.list.AdapterBrakeChet
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.list.ListBrakeFragmentChet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListCustomFieldFragmentChet : Fragment() {
    private lateinit var binding: FragmentListCustomFieldChetBinding
    private lateinit var myDbManagerCustomField: MyDbManagerCustomField
    private val adapterCustomFieldChet = AdapterCustomFieldChet(ArrayList(), this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentListCustomFieldChetBinding.inflate(inflater, container, false)

        binding.fbAddCustomFieldChet.setOnClickListener {
            findNavController().navigate(R.id.action_listCustomFieldFragmentChet_to_addCustomFieldFragmentChet)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
    }

    override fun onResume() {
        super.onResume()
        myDbManagerCustomField.openDb()
        fillAdapter()
    }

    private fun init(v: View) = with(binding) {
        myDbManagerCustomField = MyDbManagerCustomField(v.context)
        rcCustomFieldChet.layoutManager = LinearLayoutManager(requireContext())
        val swapHelper = getSwapMg()
        swapHelper.attachToRecyclerView(rcCustomFieldChet)
        rcCustomFieldChet.adapter = adapterCustomFieldChet
    }

    private fun fillAdapter() {
        CoroutineScope(Dispatchers.Main).launch {
            adapterCustomFieldChet.updateCustomFieldChet(myDbManagerCustomField.readDbDataCustomFieldChet())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myDbManagerCustomField.closeDb()
    }

    private fun getSwapMg() : ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapterCustomFieldChet.removeItemCustomFieldChet(viewHolder.adapterPosition, myDbManagerCustomField)
            }

        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = ListCustomFieldFragmentChet()
    }
}