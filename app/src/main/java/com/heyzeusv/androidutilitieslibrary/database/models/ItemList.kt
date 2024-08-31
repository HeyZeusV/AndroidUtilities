package com.heyzeusv.androidutilitieslibrary.database.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class ItemList(
    @PrimaryKey(autoGenerate = true)
    val itemListId: Long = 0L,
    val name: String = "",
)

data class ItemListWithItems(
    @Embedded val itemList: ItemList = ItemList(0, ""),
    @Relation(
        parentColumn = "itemListId",
        entityColumn = "parentItemListId"
    )
    val items: List<Item> = emptyList()
) {
    @Ignore
    private val numOfCheckedItems = items.count { it.isChecked }
    @Ignore
    private val numOfItems = items.size
    @Ignore
    private val progressFloat = if (numOfItems == 0) {
        0f
    } else {
        numOfCheckedItems.toFloat() / numOfItems
    }
    @Ignore
    private val progressString = "$numOfCheckedItems/$numOfItems"
    @Ignore
    val progress = Pair(progressFloat, progressString)
}