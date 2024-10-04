package com.heyzeusv.androidutilitieslibrary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.androidutilitieslibrary.database.models.Item
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ItemDao : BaseDao<Item>("an_item_table_name") {

    @Query("SELECT * " +
            "FROM an_item_table_name")
    abstract fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * " +
            "FROM an_item_table_name " +
            "JOIN ItemFts ON an_item_table_name.name = ItemFts.name " +
            "WHERE ItemFts MATCH :query")
    abstract fun searchItems(query: String): Flow<List<Item>>
}