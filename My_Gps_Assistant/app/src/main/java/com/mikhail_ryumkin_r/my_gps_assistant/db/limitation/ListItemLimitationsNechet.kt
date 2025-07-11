package com.mikhail_ryumkin_r.my_gps_assistant.db.limitation

import android.os.Parcel
import android.os.Parcelable

class ListItemLimitationsNechet(): Parcelable {
    var idNechet: Int = 0
    var startNechet: Int = 0
    var picketStartNechet: Int = 0
    var finishNechet: Int = 0
    var picketFinishNechet: Int = 0
    var speedNechet: Int = 0
    var switchNechetAdd: Int = 0

    constructor(parcel: Parcel) : this() {
        startNechet = parcel.readInt()
        picketStartNechet = parcel.readInt()
        finishNechet = parcel.readInt()
        picketFinishNechet = parcel.readInt()
        speedNechet = parcel.readInt()
        switchNechetAdd = parcel.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(startNechet)
        dest.writeInt(picketStartNechet)
        dest.writeInt(finishNechet)
        dest.writeInt(picketFinishNechet)
        dest.writeInt(speedNechet)
        dest.writeInt(switchNechetAdd)
    }

    companion object CREATOR : Parcelable.Creator<ListItemLimitationsNechet> {
        override fun createFromParcel(parcel: Parcel): ListItemLimitationsNechet {
            return ListItemLimitationsNechet(parcel)
        }

        override fun newArray(size: Int): Array<ListItemLimitationsNechet?> {
            return arrayOfNulls(size)
        }
    }
}