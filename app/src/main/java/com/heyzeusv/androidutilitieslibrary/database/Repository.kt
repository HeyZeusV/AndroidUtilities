package com.heyzeusv.androidutilitieslibrary.database

import androidx.sqlite.db.SimpleSQLiteQuery
import com.heyzeusv.androidutilitieslibrary.database.dao.AllDao
import com.heyzeusv.androidutilitieslibrary.database.dao.CategoryDao
import com.heyzeusv.androidutilitieslibrary.database.dao.DefaultItemDao
import com.heyzeusv.androidutilitieslibrary.database.dao.ItemDao
import com.heyzeusv.androidutilitieslibrary.database.dao.ItemListDao
import com.heyzeusv.androidutilitieslibrary.database.models.Category
import com.heyzeusv.androidutilitieslibrary.database.models.DefaultItem
import com.heyzeusv.androidutilitieslibrary.database.models.Item
import com.heyzeusv.androidutilitieslibrary.database.models.ItemList
import com.heyzeusv.androidutilitieslibrary.database.models.ItemListWithItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Repository @Inject constructor(
    private val transactionProvider: TransactionProvider,
    private val allDao: AllDao,
    private val itemListDao: ItemListDao,
    private val itemDao: ItemDao,
    private val defaultItemDao: DefaultItemDao,
    private val categoryDao: CategoryDao,
) {
    /**
     *  All Queries
     */
    suspend fun deleteAll() {
        itemDao.deleteAll()
        itemListDao.deleteAll()
        defaultItemDao.deleteAll()
        categoryDao.deleteAll()
        allDao.deleteAllPrimaryKeys()
    }

    suspend fun insertRoomData(data: RoomData) {
        categoryDao.insert(*data.categoryData.toTypedArray())
        defaultItemDao.insert(*data.defaultItemData.toTypedArray())
        itemListDao.insert(*data.itemListData.toTypedArray())
        itemDao.insert(*data.itemData.toTypedArray())
    }

    suspend fun getAllRoomData(): RoomData = RoomData(
        categoryData = categoryDao.getAll(),
        itemListData = itemListDao.getAll(),
        defaultItemData = defaultItemDao.getAll(),
        itemData = itemDao.getAll(),
    )

    suspend fun rebuildDefaultItemFts() = allDao.rebuildDefaultItemFts()

    suspend fun callCheckpoint(): Int =
        withContext(Dispatchers.IO) {
            val query = SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE)")
            allDao.callCheckpoint(query)
        }

    /**
     *  ItemList Queries
     */
    suspend fun insertItemList(vararg itemLists: ItemList) =
        withContext(Dispatchers.IO) { itemListDao.insert(*itemLists) }

    suspend fun updateItemList(vararg itemLists: ItemList) =
        withContext(Dispatchers.IO) { itemListDao.update(*itemLists) }

    suspend fun deleteItemList(vararg itemLists: ItemList) =
        withContext(Dispatchers.IO) { itemListDao.delete(*itemLists) }

    suspend fun copyItemListWithItems(itemList: ItemList, items: List<Item>) =
        withContext(Dispatchers.IO) {
            itemListDao.insert(itemList)
            itemDao.insert(*items.toTypedArray())
        }

    fun getItemListWithId(id: Long): Flow<ItemList> = itemListDao.getItemListWithId(id)


    fun getAllItemListsWithoutId(id: Long): Flow<List<ItemListWithItems>> =
        itemListDao.getAllItemListsWithoutId(id)

    fun getItemListWithItemsWithId(id: Long): Flow<ItemListWithItems?> =
        itemListDao.getItemListWithItemsWithId(id)

    fun getMaxItemListId(): Flow<Long?> = itemListDao.getMaxItemListId()

    /**
     *  Item Queries
     */
    suspend fun insertItems(vararg items: Item) =
        withContext(Dispatchers.IO) { itemDao.insert(*items) }

    suspend fun updateItems(vararg items: Item) =
        withContext(Dispatchers.IO) { itemDao.update(*items) }

    suspend fun deleteItems(vararg items: Item) =
        withContext(Dispatchers.IO) { itemDao.delete(*items) }

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