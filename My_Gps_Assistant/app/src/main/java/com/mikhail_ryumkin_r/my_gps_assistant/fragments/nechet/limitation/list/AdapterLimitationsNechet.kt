package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.limitation.list

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.ItemLimitationsNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.limitation.ListItemLimitationsNechet
import com.mikhail_ryumkin_r.my_gps_assistant.db.limitation.MyDbManagerLimitations

class AdapterLimitationsNechet(listMain: ArrayList<ListItemLimitationsNechet>, private var listLimitationsFragmentNechet: ListLimitationsFragmentNechet) : RecyclerView.Adapter<AdapterLimitationsNechet.MyViewHolder>() {
    private var listArray = listMain

    class MyViewHolder(view: View, context: ListLimitationsFragmentNechet) : RecyclerView.ViewHolder(view) {
        private val binding = ItemLimitationsNechetBinding.bind(view)

        @SuppressLint("UseKtx")
        fun setData(item: ListItemLimitationsNechet) = with(binding){
            kmStartItemLimitationsNechet.text = item.startNechet.toString()
            pkStartItemLimitationsNechet.text = item.picketStartNechet.toString()
            kmFinishItemLimitationsNechet.text = item.finishNechet.toString()
            pkFinishItemLimitationsNechet.text = item.picketFinishNechet.toString()
            speedItemLimitationsNechet.text = item.speedNechet.toString()
            limitationCheckNechetItem.text = item.switchNechetAdd.toString()

            if (limitationCheckNechetItem.text == "1") {
                idItemLayoutLimitationsNechet.setCardBackgroundColor(Color.parseColor(
                    PreferenceManager.getDefaultSharedPreferences(itemView.context)
                        .getString("color_limitation_time_key", "#FF009EDA")))
            } else {
                idItemLayoutLimitationsNechet.setCardBackgroundColor(Color.parseColor(
                    PreferenceManager.getDefaultSharedPreferences(itemView.context)
                        .getString("color_limitation_key", "#FF009EDA")))
            }

            idItemLayoutLimitationsNechet.setOnClickListener {
                val action = ListLimitationsFragmentNechetDirections.actionListLimitationsFragmentNechetToUpdateLimitationsFragmentNechet(item)
                idItemLayoutLimitationsNechet.findNavController().navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater.inflate(R.layout.item_limitations_nechet, parent, false), listLimitationsFragmentNechet)
    }

    override fun getItemCount(): Int {
        return listArray.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(listArray[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateLimitationsNechet(listItems: List<ListItemLimitationsNechet>){
        listArray.clear()
        listArray.addAll(listItems)
        listArray.sortBy { it.speedNechet }
        listArray.sortByDescending { it.picketStartNechet }
        listArray.sortByDescending { it.startNechet }
        notifyDataSetChanged()
    }

    fun removeItemLimitationsNechet(pos: Int, dbManagerLimitations: MyDbManagerLimitations){
        dbManagerLimitations.deleteDbDataLimitationsNechet(listArray[pos].idNechet)
        listArray.removeAt(pos)
        notifyItemRangeChanged(0, listArray.size)
        notifyItemRemoved(pos)
    }
}