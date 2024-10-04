package com.heyzeusv.androidutilitieslibrary.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.room.Update
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
abstract class BaseDao<T>(private val tableName: String) {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(vararg entities: T)

    @Update
    abstract suspend fun update(vararg entities: T)

    @Delete
    abstract suspend fun delete(vararg entities: T)

    @Upsert
    abstract suspend fun upsert(vararg entities: T)

    @RawQuery
    protected abstract suspend fun deleteAll(query: SupportSQLiteQuery): Int

    suspend fun deleteAll() {
        val query = SimpleSQLiteQuery("DELETE FROM $tableName")
        deleteAll(query)
    }

    @RawQuery
    protected abstract suspend fun getAll(query: SupportSQLiteQuery): List<T>

    suspend fun getAll(): List<T> {
        val query = SimpleSQLiteQuery("SELECT * FROM $tableName")
        return getAll(query)
    }
}