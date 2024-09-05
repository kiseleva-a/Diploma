package ru.netology.nework.entity.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.UserPreview

class UserMapConverter {
    @TypeConverter
    fun fromUsers(users: Map<Int,UserPreview>): String {
        return Gson().toJson(users)
    }

    @TypeConverter
    fun toUsers(data: String):Map<Int,UserPreview> {
        val mapType = object : TypeToken<Map<Int, UserPreview>>() {
        }.type
        return Gson().fromJson(data, mapType)
    }
}