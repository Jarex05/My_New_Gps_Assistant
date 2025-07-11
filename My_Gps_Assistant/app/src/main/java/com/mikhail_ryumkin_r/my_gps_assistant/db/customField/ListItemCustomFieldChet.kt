package com.mikhail_ryumkin_r.my_gps_assistant.db.customField

import android.os.Parcel
import android.os.Parcelable

class ListItemCustomFieldChet(): Parcelable {
    var idChetCustom: Int = 0
    var startChetCustom: Int = 0
    var picketStartChetCustom: Int = 0
    var fieldChetCustom: String = ""

    constructor(parcel: Parcel) : this() {
        idChetCustom = parcel.readInt()
        startChetCustom = parcel.readInt()
        picketStartChetCustom = parcel.readInt()
        fieldChetCustom = parcel.readString().toString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(startChetCustom)
        dest.writeInt(picketStartChetCustom)
        dest.writeString(fieldChetCustom)
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