package com.mikhail_ryumkin_r.my_gps_assistant.db.brake

import android.os.Parcel
import android.os.Parcelable

class ListItemBrakeChet(): Parcelable {
    var idChet: Int = 0
    var startChet: Int = 0
    var picketStartChet: Int = 0
    var switchChetAdd: Int = 0

    constructor(parcel: Parcel) : this() {
        startChet = parcel.readInt()
        picketStartChet = parcel.readInt()
        switchChetAdd = parcel.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(startChet)
        dest.writeInt(picketStartChet)
        dest.writeInt(switchChetAdd)
    }

    companion object CREATOR : Parcelable.Creator<ListItemBrakeChet> {
        override fun createFromParcel(parcel: Parcel): ListItemBrakeChet {
            return ListItemBrakeChet(parcel)
        }

        override fun newArray(size: Int): Array<ListItemBrakeChet?> {
            return arrayOfNulls(size)
        }
    }
}