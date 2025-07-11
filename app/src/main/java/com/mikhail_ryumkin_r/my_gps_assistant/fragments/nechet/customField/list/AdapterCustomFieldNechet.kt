package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.customField.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.ItemCustomFieldNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.customField.ListItemCustomFieldNechet
import com.mikhail_ryumkin_r.my_gps_assistant.db.customField.MyDbManagerCustomField

@Suppress("DEPRECATION")
class AdapterCustomFieldNechet(listMain: ArrayList<ListItemCustomFieldNechet>, listCustomFieldFragmentNechet: ListCustomFieldFragmentNechet) : RecyclerView.Adapter<AdapterCustomFieldNechet.MyViewHolder>() {
    private var listArray = listMain
    private var listCustomFieldFragment = listCustomFieldFragmentNechet

    class MyViewHolder(view: View, context: ListCustomFieldFragmentNechet) : RecyclerView.ViewHolder(view) {
        private val binding = ItemCustomFieldNechetBinding.bind(view)

        @SuppressLint("UseKtx")
        fun setData(item: ListItemCustomFieldNechet) = with(binding){
            kmStartItemCustomFieldNechet.text = item.startNechetCustom.toString()
            pkStartItemCustomFieldNechet.text = item.picketStartNechetCustom.toString()
            tvCustomFieldNechet.text = item.fieldNechetCustom

            idItemLayoutCustomFieldNechet.setOnClickListener {
                val action = ListCustomFieldFragmentNechetDirections.actionListCustomFieldFragmentNechetToUpdateCustomFieldFragmentNechet(item)
                idItemLayoutCustomFieldNechet.findNavController().navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater.inflate(R.layout.item_custom_field_nechet, parent, false), listCustomFieldFragment)
    }

    override fun getItemCount(): Int {
        return listArray.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(listArray[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCustomFieldNechet(listItems: List<ListItemCustomFieldNechet>){
        listArray.clear()
        listArray.addAll(listItems)
        listArray.sortByDescending { it.picketStartNechetCustom }
        listArray.sortByDescending { it.startNechetCustom }
        notifyDataSetChanged()
    }

    fun removeItemCustomFieldNechet(pos: Int, dbManagerCustomField: MyDbManagerCustomField){
        dbManagerCustomField.deleteDbDataCustomFieldNechet(listArray[pos].idNechetCustom)
        listArray.removeAt(pos)
        notifyItemRangeChanged(0, listArray.size)
        notifyItemRemoved(pos)
    }
}