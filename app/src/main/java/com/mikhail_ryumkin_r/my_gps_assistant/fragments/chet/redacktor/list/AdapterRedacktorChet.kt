package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.redacktor.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.ItemRedacktorChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.redacktor.ListItemRedacktorChet
import com.mikhail_ryumkin_r.my_gps_assistant.db.redacktor.MyDbManagerRedacktor

class AdapterRedacktorChet(listMain: ArrayList<ListItemRedacktorChet>, private var listRedacktorFragmentChet: ListRedacktorFragmentChet) : RecyclerView.Adapter<AdapterRedacktorChet.MyViewHolder>() {
    private var listArray = listMain

    class MyViewHolder(view: View, context: ListRedacktorFragmentChet) : RecyclerView.ViewHolder(view) {
        private val binding = ItemRedacktorChetBinding.bind(view)

        fun setData(item: ListItemRedacktorChet) = with(binding){
            kmItemRedacktorChet.text = item.startChet.toString()
            pkItemRedacktorChet.text = item.picketStartChet.toString()
            minusItemRedactorChet.text = item.minusChet
            plusItemRedacktorChet.text = item.plusChet

            idItemLayoutRedacktorChet.setOnClickListener {
                val action = ListRedacktorFragmentChetDirections.actionListRedacktorFragmentChetToUpdateRedacktorFragmentChet(item)
                idItemLayoutRedacktorChet.findNavController().navigate(action)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater.inflate(R.layout.item_redacktor_chet, parent, false), listRedacktorFragmentChet)
    }

    override fun getItemCount(): Int {
        return listArray.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(listArray[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapterChet(listItems: List<ListItemRedacktorChet>){
        listArray.clear()
        listArray.addAll(listItems)
        listArray.sortBy { it.picketStartChet }
        listArray.sortBy { it.startChet }
        notifyDataSetChanged()
    }

    fun removeItemChet(pos: Int, dbManagerRedacktor: MyDbManagerRedacktor){
        dbManagerRedacktor.deleteDbDataRedactorChet(listArray[pos].idChet)
        listArray.removeAt(pos)
        notifyItemRangeChanged(0, listArray.size)
        notifyItemRemoved(pos)
    }
}