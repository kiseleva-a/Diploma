package ru.netology.nework.entity.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class IdListConverter {

    @TypeConverter
    fun fromIds(ids: List<Int>) : String{
        return  Gson().toJson(ids)
    }

    @TypeConverter
    fun toIds(data: String) : List<Int>{
        val listType = object : TypeToken<List<Int>>() {
        }.type
        return Gson().fromJson(data,listType)
    }
}