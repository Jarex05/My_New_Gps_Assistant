package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.pantograph.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.ItemPantographNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.pantograph.ListItemPantographNechet
import com.mikhail_ryumkin_r.my_gps_assistant.db.pantograph.MyDbManagerPantograph

class AdapterPantographNechet(listMain: ArrayList<ListItemPantographNechet>, private var listPantographFragmentNechet: ListPantographFragmentNechet) : RecyclerView.Adapter<AdapterPantographNechet.MyViewHolder>() {
    private var listArray = listMain

    class MyViewHolder(view: View, context: ListPantographFragmentNechet) : RecyclerView.ViewHolder(view) {
        private val binding = ItemPantographNechetBinding.bind(view)

        fun setData(item: ListItemPantographNechet) = with(binding){
            kmStartItemPantographNechet.text = item.startNechet.toString()
            pkStartItemPantographNechet.text = item.picketStartNechet.toString()

            idItemLayoutPantographNechet.setOnClickListener {
                val action = ListPantographFragmentNechetDirections.actionListPantographFragmentNechetToUpdatePantographFragmentNechet(item)
                idItemLayoutPantographNechet.findNavController().navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater.inflate(R.layout.item_pantograph_nechet, parent, false), listPantographFragmentNechet)
    }

    override fun getItemCount(): Int {
        return listArray.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(listArray[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePantographNechet(listItems: List<ListItemPantographNechet>){
        listArray.clear()
        listArray.addAll(listItems)
        listArray.sortByDescending { it.picketStartNechet }
        listArray.sortByDescending { it.startNechet }
        notifyDataSetChanged()
    }

    fun removeItemPantographNechet(pos: Int, dbManagerPantograph: MyDbManagerPantograph){
        dbManagerPantograph.deleteDbDataPantographNechet(listArray[pos].idNechet)
        listArray.removeAt(pos)
        notifyItemRangeChanged(0, listArray.size)
        notifyItemRemoved(pos)
    }
}