package com.mikhail_ryumkin_r.my_gps_assistant.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Toast
import com.mikhail_ryumkin_r.my_gps_assistant.R
import com.mikhail_ryumkin_r.my_gps_assistant.databinding.SaveDialogBinding
import com.mikhail_ryumkin_r.my_gps_assistant.dbRoom.TrackItemChet

object DialogManager {

    // ************************************************************

    fun showLocEnableDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.location_disabled)
        dialog.setMessage(context.getString(R.string.location_dialog_message))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Да"){
                _, _ -> listener.onClick()
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Нет"){
                _, _ -> dialog.dismiss()
        }
        dialog.show()
    }

    interface Listener{
        fun onClick()
    }

    // ************************************************************

    fun showLocEnableDialogRecording(context: Context, listenerRecording: ListenerRecording){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.location_disabled)
        dialog.setMessage(context.getString(R.string.location_dialog_message))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Да"){
                _, _ -> listenerRecording.onClickRecording()
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Нет"){
                _, _ -> dialog.dismiss()
        }
        dialog.show()
    }

    interface ListenerRecording{
        fun onClickRecording()
    }

    // ************************************************************

    @SuppressLint("UseKtx")
    fun chowSaveDialog(context: Context, item: TrackItemChet?, listenerTrack: ListenerTrack){
        val builder = AlertDialog.Builder(context)
        val binding = SaveDialogBinding.inflate(LayoutInflater.from(context), null, false)
        builder.setView(binding.root)
        val dialogSave = builder.create()
        binding.apply {

            val distance = "${item?.distance} км"
            tvDistance.text = distance

            bSave.setOnClickListener {
                val dialogTitleTrackChet = binding.idTrackTitle.text.toString()
                if (dialogTitleTrackChet == ""){
                    Toast.makeText(context, "Введите название маршрута!", Toast.LENGTH_LONG).show()
                } else {
                    item?.title = dialogTitleTrackChet
                    listenerTrack.onClickTrack()
                    dialogSave.dismiss()
                }
//                listenerTrack.onClickTrack()
//                dialogSave.dismiss()
            }
            bCancel.setOnClickListener {
                dialogSave.dismiss()
            }
            dialogSave.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogSave.show()
        }
    }

    interface ListenerTrack{
        fun onClickTrack()
    }

    // ************************************************************
}