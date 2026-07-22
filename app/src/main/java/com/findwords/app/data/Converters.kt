package com.findwords.app.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value == null) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        if (list == null) return null
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromIntArray(value: String?): IntArray {
        if (value == null) return intArrayOf()
        val type = object : TypeToken<IntArray>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun toIntArray(array: IntArray?): String? {
        if (array == null) return null
        return Gson().toJson(array)
    }
}