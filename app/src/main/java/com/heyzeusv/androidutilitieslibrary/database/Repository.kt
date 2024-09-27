package com.heyzeusv.androidutilitieslibrary.database

import androidx.sqlite.db.SimpleSQLiteQuery
import com.heyzeusv.androidutilitieslibrary.database.dao.AllDao
import com.heyzeusv.androidutilitieslibrary.database.dao.CategoryDao
import com.heyzeusv.androidutilitieslibrary.database.dao.ItemDao
import com.heyzeusv.androidutilitieslibrary.database.models.Category
import com.heyzeusv.androidutilitieslibrary.database.models.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Repository @Inject constructor(
    val transactionProvider: TransactionProvider,
    private val allDao: AllDao,
    private val itemDao: ItemDao,
    private val categoryDao: CategoryDao,
) {
    /**
     *  All Queries
     */
    suspend fun deleteAll() {
        itemDao.deleteAll()
        categoryDao.deleteAll()
        allDao.deleteAllPrimaryKeys()
    }

    suspend fun insertRoomData(data: RoomData) {
        categoryDao.insert(*data.categoryData.toTypedArray())
        itemDao.insert(*data.itemData.toTypedArray())
    }

    suspend fun getAllRoomData(): RoomData = RoomData(
        categoryData = categoryDao.getAll(),
        itemData = itemDao.getAll(),
    )

    suspend fun rebuildItemFts() = allDao.rebuildItemFts()

    suspend fun callCheckpoint(): Int =
        withContext(Dispatchers.IO) {
            val query = SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE)")
            allDao.callCheckpoint(query)
        }

    /**
     *  Item Queries
     */
    suspend fun upsertItems(vararg items: Item) =
        withContext(Dispatchers.IO) { itemDao.upsert(*items) }

    suspend fun deleteItems(vararg items: Item) =
        withContext(Dispatchers.IO) { itemDao.delete(*items) }

    fun getAllItems(): Flow<List<Item>> =
        itemDao.getAllItems()

    fun searchItems(query: String): Flow<List<Item>> =
        itemDao.searchItems(query)

    /**
     *  Category Queries
     */
    suspend fun insertCategories(vararg categories: Category) =
        withContext(Dispatchers.IO) { categoryDao.insert(*categories) }

    fun getAllCategoriesFlow(): Flow<List<Category>> = categoryDao.getAllCategories()
}