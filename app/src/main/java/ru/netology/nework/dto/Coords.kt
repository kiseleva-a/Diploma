package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName

data class Coords(
    @SerializedName("lat")
    val lat: String,
    @SerializedName("long")
    val long: String,
){
    override fun toString(): String {
        return "$lat | $long"
    }
}
