package com.heyzeusv.androidutilitieslibrary.database

import androidx.room.TypeConverter
import java.util.Date

class RoomTypeConverters {
    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
}