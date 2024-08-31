package com.heyzeusv.androidutilitieslibrary.database.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["name"],
            childColumns = ["category"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemList::class,
            parentColumns = ["itemListId"],
            childColumns = ["parentItemListId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["category"],
            name = "index_item_category_name"
        ),
        Index(
            value = ["parentItemListId"],
            name = "index_parent_id"
        )
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = true)
    override val itemId: Long = 0L,
    override val name: String = "",
    val isChecked: Boolean = false,
    override val category: String = "",
    override val quantity: Double = 0.0,
    override val unit: String = "",
    override val memo: String = "",
    val parentItemListId: Long = 0L,
    val originItemListId: Long? = null,
) : BaseItem {

    override fun editCopy(
        itemId: Long,
        name: String,
        category: String,
        quantity: Double,
        unit: String,
        memo: String
    ): Item {
        return Item(
            itemId = itemId,
            name = name,
            isChecked = this.isChecked,
            category = category,
            quantity = quantity,
            unit = unit,
            memo = memo,
            parentItemListId = this.parentItemListId,
            originItemListId = this.originItemListId,
        )
    }
}