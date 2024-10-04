package com.heyzeusv.androidutilitieslibrary.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
abstract class AllDao {

    @Query("INSERT INTO ItemFts(ItemFts) VALUES ('rebuild')")
    abstract suspend fun rebuildItemFts()

    @Query("DELETE FROM sqlite_sequence")
    abstract suspend fun deleteAllPrimaryKeys()

    @RawQuery
    abstract suspend fun callCheckpoint(query: SupportSQLiteQuery): Int
}