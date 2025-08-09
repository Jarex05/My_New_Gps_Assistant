package com.mikhail_ryumkin_r.my_gps_assistant.fragments.chet.limitation.list

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.ItemLimitationsChetBinding
import com.mikhail_ryumkin_r.my_gps_assistant.db.limitation.ListItemLimitationsChet
import com.mikhail_ryumkin_r.my_gps_assistant.db.limitation.MyDbManagerLimitations
import com.mikhail_ryumkin_r.my_gps_assistant.utils.SharedPref
import kotlin.collections.ArrayList

class AdapterLimitationsChet(listMain: ArrayList<ListItemLimitationsChet>, private var listLimitationsFragmentChet: ListLimitationsFragmentChet) : RecyclerView.Adapter<AdapterLimitationsChet.MyViewHolder>() {
    private var listArray = listMain
    private val diffUtilCallback : DiffUtil.ItemCallback<ListItemLimitationsChet> = object : DiffUtil.ItemCallback<ListItemLimitationsChet>() {
        override fun areItemsTheSame(
            oldItem: ListItemLimitationsChet,
            newItem: ListItemLimitationsChet
        ): Boolean {
            return oldItem === newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: ListItemLimitationsChet,
            newItem: ListItemLimitationsChet
        ): Boolean {
            return oldItem == newItem
        }

    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtilCallback)

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

//    @SuppressLint("NotifyDataSetChanged")
    fun updateLimitationsChet(listItems: List<ListItemLimitationsChet>){
        listArray.clear()
        listArray.addAll(listItems)
        listArray.sortBy { it.speedChet }
        listArray.sortBy { it.picketStartChet }
        listArray.sortBy { it.startChet }
//        notifyDataSetChanged()
        asyncListDiffer.submitList(listItems)
    }

    fun removeItemLimitationsChet(pos: Int, dbManagerLimitations: MyDbManagerLimitations){
        dbManagerLimitations.deleteDbDataLimitationsChet(listArray[pos].idChet)
        var a = 0
        var b = 0
        var c = 0
        var d = 0
        var e = 0
        a = listArray[pos].speedChet
        b = listArray[pos].startChet
        c = listArray[pos].picketStartChet
        d = listArray[pos].finishChet
        e = listArray[pos].picketFinishChet
        listLimitationsFragmentChet.view?.let { SharedPref.Companion.setValueDeleteSpeed(it.context, a) }
        listLimitationsFragmentChet.view?.let { SharedPref.Companion.setValueDeleteKmStart(it.context, b) }
        listLimitationsFragmentChet.view?.let { SharedPref.Companion.setValueDeleteKmFinish(it.context, d) }
        listLimitationsFragmentChet.view?.let { SharedPref.Companion.setValueDeletePkStart(it.context, c) }
        listLimitationsFragmentChet.view?.let { SharedPref.Companion.setValueDeletePkFinish(it.context, e) }
        listArray.removeAt(pos)
        notifyItemRangeChanged(0, listArray.size)
        notifyItemRemoved(pos)
    }
}
