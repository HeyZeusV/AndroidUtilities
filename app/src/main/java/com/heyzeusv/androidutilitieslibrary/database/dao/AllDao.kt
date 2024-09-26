package com.heyzeusv.androidutilitieslibrary.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
abstract class AllDao {

    @Query("INSERT INTO DefaultItemFts(DefaultItemFts) VALUES ('rebuild')")
    abstract suspend fun rebuildDefaultItemFts()

    @Query("DELETE FROM sqlite_sequence")
    abstract suspend fun deleteAllPrimaryKeys()

    @RawQuery
    abstract suspend fun callCheckpoint(query: SupportSQLiteQuery): Int
}