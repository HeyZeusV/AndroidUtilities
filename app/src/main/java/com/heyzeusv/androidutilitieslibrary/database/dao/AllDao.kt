package com.heyzeusv.androidutilitieslibrary.database.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class AllDao {

    @Query("INSERT INTO DefaultItemFts(DefaultItemFts) VALUES ('rebuild')")
    abstract suspend fun rebuildDefaultItemFts()

    @Query("DELETE FROM sqlite_sequence")
    abstract suspend fun deleteAllPrimaryKeys()
}