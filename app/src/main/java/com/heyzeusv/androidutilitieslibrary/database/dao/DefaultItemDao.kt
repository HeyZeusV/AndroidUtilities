package com.heyzeusv.androidutilitieslibrary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.androidutilitieslibrary.database.models.DefaultItem
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DefaultItemDao : BaseDao<DefaultItem>("DefaultItem") {

    @Query("SELECT * " +
            "FROM DefaultItem")
    abstract fun getAllDefaultItems(): Flow<List<DefaultItem>>

    @Query("SELECT * " +
            "FROM DefaultItem " +
            "JOIN DefaultItemFts ON DefaultItem.name = DefaultItemFts.name " +
            "WHERE DefaultItemFts MATCH :query")
    abstract fun searchDefaultItems(query: String): Flow<List<DefaultItem>>
}