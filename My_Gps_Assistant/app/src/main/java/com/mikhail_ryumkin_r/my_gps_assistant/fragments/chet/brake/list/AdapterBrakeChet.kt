package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.brake.list

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.ItemBrakeChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.ListItemBrakeChet
import com.mikhail_ryumkin_r.my_gps_assistant.db.brake.MyDbManagerBrake
import com.mikhail_ryumkin_r.my_gps_assistant.R

@Suppress("DEPRECATION")
class AdapterBrakeChet(listMain: ArrayList<ListItemBrakeChet>, listBrakeFragmentChet: ListBrakeFragmentChet) : RecyclerView.Adapter<AdapterBrakeChet.MyViewHolder>() {
    private var listArray = listMain
    private var listBrakeFragment = listBrakeFragmentChet

    class MyViewHolder(view: View, context: ListBrakeFragmentChet) : RecyclerView.ViewHolder(view) {
        private val binding = ItemBrakeChetBinding.bind(view)

        @SuppressLint("UseKtx")
        fun setData(item: ListItemBrakeChet) = with(binding){
            kmStartItemBrakeChet.text = item.startChet.toString()
            pkStartItemBrakeChet.text = item.picketStartChet.toString()
            brakeCheckChetItem.text = item.switchChetAdd.toString()

            if (brakeCheckChetItem.text == "1") {
                idItemLayoutBrakeChet.setCardBackgroundColor(Color.parseColor(
                    PreferenceManager.getDefaultSharedPreferences(itemView.context)
                        .getString("color_brake_refresh_key", "#FF009EDA")))
            } else {
                idItemLayoutBrakeChet.setCardBackgroundColor(Color.parseColor(
                    PreferenceManager.getDefaultSharedPreferences(itemView.context)
                        .getString("color_brake_key", "#FF009EDA")))
            }

            idItemLayoutBrakeChet.setOnClickListener {
                val action = ListBrakeFragmentChetDirections.actionListBrakeFragmentChetToUpdateBrakeFragmentChet(item)
                idItemLayoutBrakeChet.findNavController().navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater.inflate(R.layout.item_brake_chet, parent, false), listBrakeFragment)
    }

    override fun getItemCount(): Int {
        return listArray.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setData(listArray[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateBrakeChet(listItems: List<ListItemBrakeChet>){
        listArray.clear()
        listArray.addAll(listItems)
        listArray.sortBy { it.picketStartChet }
        listArray.sortBy { it.startChet }
        notifyDataSetChanged()
    }

    fun removeItemBrakeChet(pos: Int, dbManagerBrake: MyDbManagerBrake){
        dbManagerBrake.deleteDbDataBrakeChet(listArray[pos].idChet)
        listArray.removeAt(pos)
        notifyItemRangeChanged(0, listArray.size)
        notifyItemRemoved(pos)
    }
}