package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.customField.list

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.ItemBrakeChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.ItemCustomFieldChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.ListItemBrakeChet
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.MyDbManagerBrake
import com.mikhail_ryumkin_r.my_gps_assistant.db.customField.ListItemCustomFieldChet
import com.mikhail_ryumkin_r.my_gps_assistant.db.customField.MyDbManagerCustomField
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.list.AdapterBrakeChet
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.list.ListBrakeFragmentChet
import com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.list.ListBrakeFragmentChetDirections

@Suppress("DEPRECATION")
class AdapterCustomFieldChet(listMain: ArrayList<ListItemCustomFieldChet>, listCustomFieldFragmentChet: ListCustomFieldFragmentChet) : RecyclerView.Adapter<AdapterCustomFieldChet.MyViewHolder>() {
    private var listArray = listMain
    private var listCustomFieldFragment = listCustomFieldFragmentChet

    class MyViewHolder(view: View, context: ListCustomFieldFragmentChet) : RecyclerView.ViewHolder(view) {
        private val binding = ItemCustomFieldChetBinding.bind(view)

        @SuppressLint("UseKtx")
        fun setData(item: ListItemCustomFieldChet) = with(binding){
            kmStartItemCustomFieldChet.text = item.startChetCustom.toString()
            pkStartItemCustomFieldChet.text = item.picketStartChetCustom.toString()
            tvCustomFieldChet.text = item.fieldChetCustom

            idItemLayoutCustomFieldChet.setOnClickListener {
                val action = ListCustomFieldFragmentChetDirections.actionListCustomFieldFragmentChetToUpdateCustomFieldFragmentChet(item)
                idItemLayoutCustomFieldChet.findNavController().navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater.inflate(R.layout.item_custom_field_chet, parent, false), listCustomFieldFragment)
    }

    override fun getItemCount(): Int {
        return listArray.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(listArray[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCustomFieldChet(listItems: List<ListItemCustomFieldChet>){
        listArray.clear()
        listArray.addAll(listItems)
        listArray.sortBy { it.picketStartChetCustom }
        listArray.sortBy { it.startChetCustom }
        notifyDataSetChanged()
    }

    fun removeItemCustomFieldChet(pos: Int, dbManagerCustomField: MyDbManagerCustomField){
        dbManagerCustomField.deleteDbDataCustomFieldChet(listArray[pos].idChetCustom)
        listArray.removeAt(pos)
        notifyItemRangeChanged(0, listArray.size)
        notifyItemRemoved(pos)
    }
}