package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.customField.list

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
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.FragmentListCustomFieldNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.customField.MyDbManagerCustomField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListCustomFieldFragmentNechet : Fragment() {
    private lateinit var binding: FragmentListCustomFieldNechetBinding
    private lateinit var myDbManagerCustomField: MyDbManagerCustomField
    private val adapterCustomFieldNechet = AdapterCustomFieldNechet(ArrayList(), this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentListCustomFieldNechetBinding.inflate(inflater, container, false)

        binding.fbAddCustomFieldNechet.setOnClickListener {
            findNavController().navigate(R.id.action_listCustomFieldFragmentNechet_to_addCustomFieldFragmentNechet)
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
        rcCustomFieldNechet.layoutManager = LinearLayoutManager(requireContext())
        val swapHelper = getSwapMg()
        swapHelper.attachToRecyclerView(rcCustomFieldNechet)
        rcCustomFieldNechet.adapter = adapterCustomFieldNechet
    }

    private fun fillAdapter() {
        CoroutineScope(Dispatchers.Main).launch {
            adapterCustomFieldNechet.updateCustomFieldNechet(myDbManagerCustomField.readDbDataCustomFieldNechet())
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
                adapterCustomFieldNechet.removeItemCustomFieldNechet(viewHolder.adapterPosition, myDbManagerCustomField)
            }

        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = ListCustomFieldFragmentNechet()
    }
}