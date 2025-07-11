package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.pantograph.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.ItemPantographChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.pantograph.ListItemPantographChet
import com.mikhail_ryumkin_r.my_gps_assistant.db.pantograph.MyDbManagerPantograph

class AdapterPantographChet(listMain: ArrayList<ListItemPantographChet>, private var listPantographFragmentChet: ListPantographFragmentChet) : RecyclerView.Adapter<AdapterPantographChet.MyViewHolder>() {
    private var listArray = listMain

    class MyViewHolder(view: View, context: ListPantographFragmentChet) : RecyclerView.ViewHolder(view) {
        private val binding = ItemPantographChetBinding.bind(view)

        fun setData(item: ListItemPantographChet) = with(binding){
            kmStartItemPantographChet.text = item.startChet.toString()
            pkStartItemPantographChet.text = item.picketStartChet.toString()

            idItemLayoutPantographChet.setOnClickListener {
                val action = ListPantographFragmentChetDirections.actionListPantographFragmentChetToUpdatePantographFragmentChet(item)
                idItemLayoutPantographChet.findNavController().navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater.inflate(R.layout.item_pantograph_chet, parent, false), listPantographFragmentChet)
    }

    override fun getItemCount(): Int {
        return listArray.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(listArray[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePantographChet(listItems: List<ListItemPantographChet>){
        listArray.clear()
        listArray.addAll(listItems)
        listArray.sortBy { it.picketStartChet }
        listArray.sortBy { it.startChet }
        notifyDataSetChanged()
    }

    fun removeItemPantographChet(pos: Int, dbManagerPantograph: MyDbManagerPantograph){
        dbManagerPantograph.deleteDbDataPantographChet(listArray[pos].idChet)
        listArray.removeAt(pos)
        notifyItemRangeChanged(0, listArray.size)
        notifyItemRemoved(pos)
    }
}