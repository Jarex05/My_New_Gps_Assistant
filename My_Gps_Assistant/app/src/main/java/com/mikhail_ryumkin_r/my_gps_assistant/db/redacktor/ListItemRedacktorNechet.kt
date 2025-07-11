package com.mikhail_ryumkin_r.my_gps_assistant.db.redacktor

import android.os.Parcel
import android.os.Parcelable

class ListItemRedacktorNechet(): Parcelable {

    var idNechet: Int = 0
    var startNechet: Int = 0
    var picketStartNechet: Int = 0
    var minusNechet = "empty"
    var plusNechet = "empty"

    constructor(parcel: Parcel) : this() {
        startNechet = parcel.readInt()
        picketStartNechet = parcel.readInt()
        minusNechet = parcel.readString().toString()
        plusNechet = parcel.readString().toString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(startNechet)
        dest.writeInt(picketStartNechet)
        dest.writeString(minusNechet)
        dest.writeString(plusNechet)
    }

    companion object CREATOR : Parcelable.Creator<ListItemRedacktorNechet> {
        override fun createFromParcel(parcel: Parcel): ListItemRedacktorNechet {
            return ListItemRedacktorNechet(parcel)
        }

        override fun newArray(size: Int): Array<ListItemRedacktorNechet?> {
            return arrayOfNulls(size)
        }
    }
}