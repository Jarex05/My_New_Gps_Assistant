package com.mikhail_ryumkin_r.my_gps_assistant.fragments.nechet.brake.list

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.ItemBrakeNechetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.ListItemBrakeNechet
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.MyDbManagerBrake

@Suppress("DEPRECATION")
class AdapterBrakeNechet(listMain: ArrayList<ListItemBrakeNechet>, private var listBrakeFragmentNechet: ListBrakeFragmentNechet) : RecyclerView.Adapter<AdapterBrakeNechet.MyViewHolder>() {
    private var listArray = listMain

    class MyViewHolder(view: View, context: ListBrakeFragmentNechet) : RecyclerView.ViewHolder(view) {
        private val binding = ItemBrakeNechetBinding.bind(view)

        @SuppressLint("UseKtx")
        fun setData(item: ListItemBrakeNechet) = with(binding){
            kmStartItemBrakeNechet.text = item.startNechet.toString()
            pkStartItemBrakeNechet.text = item.picketStartNechet.toString()
            brakeCheckNechetItem.text = item.switchNechetAdd.toString()

            if (brakeCheckNechetItem.text == "1") {
                idItemLayoutBrakeNechet.setCardBackgroundColor(Color.parseColor(
                    PreferenceManager.getDefaultSharedPreferences(itemView.context)
                        .getString("color_brake_refresh_key", "#FF009EDA")))
            } else {
                idItemLayoutBrakeNechet.setCardBackgroundColor(Color.parseColor(
                    PreferenceManager.getDefaultSharedPreferences(itemView.context)
                        .getString("color_brake_key", "#FF009EDA")))
            }

            idItemLayoutBrakeNechet.setOnClickListener {
                val action = ListBrakeFragmentNechetDirections.actionListBrakeFragmentNechetToUpdateBrakeFragmentNechet(item)
                idItemLayoutBrakeNechet.findNavController().navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater.inflate(R.layout.item_brake_nechet, parent, false), listBrakeFragmentNechet)
    }

    override fun getItemCount(): Int {
        return listArray.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(listArray[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateBrakeNechet(listItems: List<ListItemBrakeNechet>){
        listArray.clear()
        listArray.addAll(listItems)
        listArray.sortByDescending { it.picketStartNechet }
        listArray.sortByDescending { it.startNechet }
        notifyDataSetChanged()
    }

    fun removeItemBrakeNechet(pos: Int, dbManagerBrake: MyDbManagerBrake){
        dbManagerBrake.deleteDbDataBrakeNechet(listArray[pos].idNechet)
        listArray.removeAt(pos)
        notifyItemRangeChanged(0, listArray.size)
        notifyItemRemoved(pos)
    }
}