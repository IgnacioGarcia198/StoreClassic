package com.garcia.ignacio.storeclassic.db.typeconverter

import androidx.room.TypeConverter

class DbTypeConverters {
    @TypeConverter
    fun doubleListToString(list: List<Double>?) = list?.joinToString(",")

    @TypeConverter
    fun stringToDoubleList(listString: String?) =
        listString?.split(",")?.map { it.toDouble() }
}