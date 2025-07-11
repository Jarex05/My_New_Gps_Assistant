package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.limitation.list

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.ItemLimitationsChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.limitation.ListItemLimitationsChet
import com.mikhail_ryumkin_r.my_gps_assistant.db.limitation.MyDbManagerLimitations

class AdapterLimitationsChet(listMain: ArrayList<ListItemLimitationsChet>, private var listLimitationsFragmentChet: ListLimitationsFragmentChet) : RecyclerView.Adapter<AdapterLimitationsChet.MyViewHolder>() {
    private var listArray = listMain

    inner class MyViewHolder(view: View, context: ListLimitationsFragmentChet) : RecyclerView.ViewHolder(view) {
        private val binding = ItemLimitationsChetBinding.bind(view)

        @SuppressLint("UseKtx")
        fun setData(item: ListItemLimitationsChet) = with(binding){
            kmStartItemLimitationsChet.text = item.startChet.toString()
            pkStartItemLimitationsChet.text = item.picketStartChet.toString()
            kmFinishItemLimitationsChet.text = item.finishChet.toString()
            pkFinishItemLimitationsChet.text = item.picketFinishChet.toString()
            speedItemLimitationsChet.text = item.speedChet.toString()
            limitationCheckChetItem.text = item.switchChetAdd.toString()

            if (limitationCheckChetItem.text == "1") {
                idItemLayoutLimitationsChet.setCardBackgroundColor(Color.parseColor(
                    PreferenceManager.getDefaultSharedPreferences(itemView.context)
                        .getString("color_limitation_time_key", "#FF009EDA")))
            } else {
                idItemLayoutLimitationsChet.setCardBackgroundColor(Color.parseColor(
                    PreferenceManager.getDefaultSharedPreferences(itemView.context)
                        .getString("color_limitation_key", "#FF009EDA")))
            }

            idItemLayoutLimitationsChet.setOnClickListener {
                val action = ListLimitationsFragmentChetDirections.actionListLimitationsFragmentChetToUpdateLimitationsFragmentChet(item)
                idItemLayoutLimitationsChet.findNavController().navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater.inflate(R.layout.item_limitations_chet, parent, false), listLimitationsFragmentChet)
    }

    override fun getItemCount(): Int {
        return listArray.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(listArray[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateLimitationsChet(listItems: List<ListItemLimitationsChet>){
        listArray.clear()
        listArray.addAll(listItems)
        listArray.sortBy { it.speedChet }
        listArray.sortBy { it.picketStartChet }
        listArray.sortBy { it.startChet }
        notifyDataSetChanged()
    }

    fun removeItemLimitationsChet(pos: Int, dbManagerLimitations: MyDbManagerLimitations){
        dbManagerLimitations.deleteDbDataLimitationsChet(listArray[pos].idChet)
        listArray.removeAt(pos)
        notifyItemRangeChanged(0, listArray.size)
        notifyItemRemoved(pos)
    }
}