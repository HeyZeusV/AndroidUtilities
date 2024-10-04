package com.heyzeusv.androidutilitieslibrary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heyzeusv.androidutilitieslibrary.database.dao.AllDao
import com.heyzeusv.androidutilitieslibrary.database.dao.CategoryDao
import com.heyzeusv.androidutilitieslibrary.database.dao.ItemDao
import com.heyzeusv.androidutilitieslibrary.database.models.Category
import com.heyzeusv.androidutilitieslibrary.database.models.Item
import com.heyzeusv.androidutilitieslibrary.database.models.ItemFts

@Database(
    entities = [
        Item::class,
        ItemFts::class,
        Category::class
    ],
    version = 5,
    exportSchema = true,
)
@TypeConverters(RoomTypeConverters::class)
abstract class Database : RoomDatabase() {
    abstract fun allDao(): AllDao
    abstract fun itemDao(): ItemDao
    abstract fun categoryDao(): CategoryDao
}