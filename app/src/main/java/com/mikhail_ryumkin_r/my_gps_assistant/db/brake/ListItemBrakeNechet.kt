package com.mikhail_ryumkin_r.my_gps_assistant.db.brake

import android.os.Parcel
import android.os.Parcelable

class ListItemBrakeNechet(): Parcelable {

    var idNechet: Int = 0
    var startNechet: Int = 0
    var picketStartNechet: Int = 0
    var fieldNechetCustom: String = ""

    constructor(parcel: Parcel) : this() {
        startNechet = parcel.readInt()
        picketStartNechet = parcel.readInt()
        fieldNechetCustom = parcel.readString().toString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(startNechet)
        dest.writeInt(picketStartNechet)
        dest.writeString(fieldNechetCustom)
    }

    companion object CREATOR : Parcelable.Creator<ListItemBrakeNechet> {
        override fun createFromParcel(parcel: Parcel): ListItemBrakeNechet {
            return ListItemBrakeNechet(parcel)
        }

        override fun newArray(size: Int): Array<ListItemBrakeNechet?> {
            return arrayOfNulls(size)
        }
    }
}