/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object Converters {
    @TypeConverter
    fun fromString(value: String?): FloatArray {
        val listType: Type = object : TypeToken<FloatArray>(){}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromFloatArray(array: FloatArray): String {
        val gson = Gson()
        return gson.toJson(array)
    }
}