package com.mikhail_ryumkin_r.my_gps_assistant.db.limitation

import android.os.Parcel
import android.os.Parcelable

class ListItemLimitationsChet(): Parcelable {
    var idChet: Int = 0
    var startChet: Int = 0
    var picketStartChet: Int = 0
    var finishChet: Int = 0
    var picketFinishChet: Int = 0
    var speedChet: Int = 0
    var switchChetAdd: Int = 0

    constructor(parcel: Parcel) : this() {
        startChet = parcel.readInt()
        picketStartChet = parcel.readInt()
        finishChet = parcel.readInt()
        picketFinishChet = parcel.readInt()
        speedChet = parcel.readInt()
        switchChetAdd = parcel.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(startChet)
        dest.writeInt(picketStartChet)
        dest.writeInt(finishChet)
        dest.writeInt(picketFinishChet)
        dest.writeInt(speedChet)
        dest.writeInt(switchChetAdd)
    }

    companion object CREATOR : Parcelable.Creator<ListItemLimitationsChet> {
        override fun createFromParcel(parcel: Parcel): ListItemLimitationsChet {
            return ListItemLimitationsChet(parcel)
        }

        override fun newArray(size: Int): Array<ListItemLimitationsChet?> {
            return arrayOfNulls(size)
        }
    }
}