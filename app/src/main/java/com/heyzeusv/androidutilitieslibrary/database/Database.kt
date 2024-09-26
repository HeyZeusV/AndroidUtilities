package com.heyzeusv.androidutilitieslibrary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heyzeusv.androidutilitieslibrary.database.dao.AllDao
import com.heyzeusv.androidutilitieslibrary.database.dao.CategoryDao
import com.heyzeusv.androidutilitieslibrary.database.dao.DefaultItemDao
import com.heyzeusv.androidutilitieslibrary.database.models.Category
import com.heyzeusv.androidutilitieslibrary.database.models.DefaultItem
import com.heyzeusv.androidutilitieslibrary.database.models.DefaultItemFts

@Database(
    entities = [
        DefaultItem::class,
        DefaultItemFts::class,
        Category::class
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(RoomTypeConverters::class)
abstract class Database : RoomDatabase() {
    abstract fun allDao(): AllDao
    abstract fun defaultItemDao(): DefaultItemDao
    abstract fun categoryDao(): CategoryDao
}