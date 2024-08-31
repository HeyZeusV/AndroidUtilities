package com.heyzeusv.androidutilitieslibrary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.heyzeusv.androidutilitieslibrary.database.dao.AllDao
import com.heyzeusv.androidutilitieslibrary.database.dao.CategoryDao
import com.heyzeusv.androidutilitieslibrary.database.dao.DefaultItemDao
import com.heyzeusv.androidutilitieslibrary.database.dao.ItemDao
import com.heyzeusv.androidutilitieslibrary.database.dao.ItemListDao
import com.heyzeusv.androidutilitieslibrary.database.models.Category
import com.heyzeusv.androidutilitieslibrary.database.models.DefaultItem
import com.heyzeusv.androidutilitieslibrary.database.models.DefaultItemFts
import com.heyzeusv.androidutilitieslibrary.database.models.Item
import com.heyzeusv.androidutilitieslibrary.database.models.ItemList

@Database(
    entities = [
        DefaultItem::class,
        DefaultItemFts::class,
        Item::class,
        ItemList::class,
        Category::class
    ],
    version = 1,
    exportSchema = true,
    autoMigrations = [

    ]
)
abstract class Database : RoomDatabase() {
    abstract fun allDao(): AllDao
    abstract fun itemListDao(): ItemListDao
    abstract fun itemDao(): ItemDao
    abstract fun defaultItemDao(): DefaultItemDao
    abstract fun categoryDao(): CategoryDao
}