package com.heyzeusv.androidutilitieslibrary.database

import androidx.room.TypeConverter
import java.util.Date

class RoomTypeConverters {
    @TypeConverter
    fun toDate(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }
}