package com.heyzeusv.androidutilitieslibrary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.androidutilitieslibrary.database.models.DefaultItem
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DefaultItemDao : BaseDao<DefaultItem>("DefaultItem") {

    @Query("SELECT * " +
            "FROM custom_default_item_table_name")
    abstract fun getAllDefaultItems(): Flow<List<DefaultItem>>

    @Query("SELECT * " +
            "FROM custom_default_item_table_name " +
            "JOIN DefaultItemFts ON custom_default_item_table_name.name = DefaultItemFts.name " +
            "WHERE DefaultItemFts MATCH :query")
    abstract fun searchDefaultItems(query: String): Flow<List<DefaultItem>>
}