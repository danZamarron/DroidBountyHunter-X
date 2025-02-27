package edu.training.droidbountyhunterkotlin.models

import android.os.Parcel
import android.os.Parcelable

data class Fugitivo @JvmOverloads constructor(
    val id: Int = 0,
    val name: String = "",
    var status: Int = 0,
    var photo: String = "",
    var latitude: Double = 0.0,
    var logitude: Double = 0.0,
    var dateCapture: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeInt(status)
        parcel.writeString(photo)
        parcel.writeDouble(latitude)
        parcel.writeDouble(logitude)
        parcel.writeString(dateCapture)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Fugitivo> {
        override fun createFromParcel(parcel: Parcel): Fugitivo {
            return Fugitivo(parcel)
        }

        override fun newArray(size: Int): Array<Fugitivo?> {
            return arrayOfNulls(size)
        }
    }

}