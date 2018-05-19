package com.odd.ostrinov2

import android.os.Parcel
import android.os.Parcelable
import com.odd.ostrinov2.tools.UtilMeths

class Ost(var title: String, var show: String, var tags: String, var videoId: String) : Parcelable {
    var id: Int = 0
    var url: String
        get() = UtilMeths.idToUrl(videoId)
        set(value) {
            videoId = UtilMeths.urlToId(value)
        }

    val searchString: String
        get() = "$title, $show, $tags"

    override fun toString(): String = "Ost{title='$title, show='$show, tags='$tags"

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(show)
        dest.writeString(tags)
        dest.writeString(videoId)
        dest.writeInt(id)
    }

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
        id = parcel.readInt()
    }

    companion object CREATOR : Parcelable.Creator<Ost> {
        override fun createFromParcel(parcel: Parcel): Ost = Ost(parcel)

        override fun newArray(size: Int): Array<Ost?> = arrayOfNulls(size)
    }
}
