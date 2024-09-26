package com.heyzeusv.androidutilitieslibrary.database

import androidx.sqlite.db.SimpleSQLiteQuery
import com.heyzeusv.androidutilitieslibrary.database.dao.AllDao
import com.heyzeusv.androidutilitieslibrary.database.dao.CategoryDao
import com.heyzeusv.androidutilitieslibrary.database.dao.DefaultItemDao
import com.heyzeusv.androidutilitieslibrary.database.models.Category
import com.heyzeusv.androidutilitieslibrary.database.models.DefaultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Repository @Inject constructor(
    val transactionProvider: TransactionProvider,
    private val allDao: AllDao,
    private val defaultItemDao: DefaultItemDao,
    private val categoryDao: CategoryDao,
) {
    /**
     *  All Queries
     */
    suspend fun deleteAll() {
        defaultItemDao.deleteAll()
        categoryDao.deleteAll()
        allDao.deleteAllPrimaryKeys()
    }

    suspend fun insertRoomData(data: RoomData) {
        categoryDao.insert(*data.categoryData.toTypedArray())
        defaultItemDao.insert(*data.defaultItemData.toTypedArray())
    }

    suspend fun getAllRoomData(): RoomData = RoomData(
        categoryData = categoryDao.getAll(),
        defaultItemData = defaultItemDao.getAll(),
    )

    suspend fun rebuildDefaultItemFts() = allDao.rebuildDefaultItemFts()

    suspend fun callCheckpoint(): Int =
        withContext(Dispatchers.IO) {
            val query = SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE)")
            allDao.callCheckpoint(query)
        }

    /**
     *  DefaultItem Queries
     */
    suspend fun upsertDefaultItems(vararg defaultItems: DefaultItem) =
        withContext(Dispatchers.IO) { defaultItemDao.upsert(*defaultItems) }

    suspend fun deleteDefaultItems(vararg defaultItems: DefaultItem) =
        withContext(Dispatchers.IO) { defaultItemDao.delete(*defaultItems) }

    fun getAllDefaultItems(): Flow<List<DefaultItem>> =
        defaultItemDao.getAllDefaultItems()

    fun searchDefaultItems(query: String): Flow<List<DefaultItem>> =
        defaultItemDao.searchDefaultItems(query)

    /**
     *  Category Queries
     */
    suspend fun insertCategories(vararg categories: Category) =
        withContext(Dispatchers.IO) { categoryDao.insert(*categories) }

    fun getAllCategoriesFlow(): Flow<List<Category>> = categoryDao.getAllCategories()
}