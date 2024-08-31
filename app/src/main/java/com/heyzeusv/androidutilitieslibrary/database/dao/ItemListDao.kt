package com.heyzeusv.androidutilitieslibrary.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.heyzeusv.androidutilitieslibrary.database.models.ItemList
import com.heyzeusv.androidutilitieslibrary.database.models.ItemListWithItems
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ItemListDao : BaseDao<ItemList>("ItemList") {

    @Query("SELECT * " +
            "FROM ItemList " +
            "WHERE itemListId=(:id)")
    abstract fun getItemListWithId(id: Long): Flow<ItemList>

    @Transaction
    @Query("SELECT * " +
            "FROM ItemList " +
            "ORDER BY " +
            "CASE WHEN :byName = 1 AND :byNameOption = 'ASC' THEN name COLLATE NOCASE END ASC, " +
            "CASE WHEN :byName = 1 AND :byNameOption = 'DESC' THEN name COLLATE NOCASE END DESC")
    abstract fun getSortedItemListsWithItems(
        byName: Boolean,
        byNameOption: String
    ): Flow<List<ItemListWithItems>>

    @Transaction
    @Query("SELECT * " +
            "FROM ItemList " +
            "WHERE itemListId IS NOT (:id)")
    abstract fun getAllItemListsWithoutId(id: Long): Flow<List<ItemListWithItems>>

    @Transaction
    @Query("SELECT * " +
            "FROM ItemList " +
            "WHERE itemListId=(:id)")
    abstract fun getItemListWithItemsWithId(id: Long): Flow<ItemListWithItems?>

    @Query("SELECT MAX(itemListId) " +
            "FROM ItemList")
    abstract fun getMaxItemListId(): Flow<Long?>
}