package com.mikhail_ryumkin_r.my_gps_assistant.db.customField

import android.os.Parcel
import android.os.Parcelable

class ListItemCustomFieldNechet(): Parcelable {
    var idNechetCustom: Int = 0
    var startNechetCustom: Int = 0
    var picketStartNechetCustom: Int = 0
    var fieldNechetCustom: String = ""

    constructor(parcel: Parcel) : this() {
        idNechetCustom = parcel.readInt()
        startNechetCustom = parcel.readInt()
        picketStartNechetCustom = parcel.readInt()
        fieldNechetCustom = parcel.readString().toString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(startNechetCustom)
        dest.writeInt(picketStartNechetCustom)
        dest.writeString(fieldNechetCustom)
    }

    companion object CREATOR : Parcelable.Creator<ListItemCustomFieldChet> {
        override fun createFromParcel(parcel: Parcel): ListItemCustomFieldChet? {
            return ListItemCustomFieldChet(parcel)
        }

        override fun newArray(size: Int): Array<out ListItemCustomFieldChet?>? {
            return arrayOfNulls(size)
        }

    }
}