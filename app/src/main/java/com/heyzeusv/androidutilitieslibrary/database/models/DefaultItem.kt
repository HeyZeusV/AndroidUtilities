package com.heyzeusv.androidutilitieslibrary.database.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["name"],
        childColumns = ["category"],
        onDelete = ForeignKey.RESTRICT,
        onUpdate = ForeignKey.CASCADE
    )],
    indices = [Index(
        value = ["category"],
        name = "index_default_category_name"
    )]
)
data class DefaultItem(
    @PrimaryKey(autoGenerate = true)
    override val itemId: Long = 0L,
    override val name: String = "",
    override val category: String = "",
    override val quantity: Double = 0.0,
    override val unit: String = "",
    override val memo: String = "",
    @Embedded(prefix = "outerEmbed_")
    val testEmbed: TestEmbed = TestEmbed()
) : BaseItem {

    @Ignore
    val testIgnore: String = ""

    override fun editCopy(
        itemId: Long,
        name: String,
        category: String,
        quantity: Double,
        unit: String,
        memo: String
    ): DefaultItem {
        return DefaultItem(
            itemId = itemId,
            name = name,
            category = category,
            quantity = quantity,
            unit = unit,
            memo = memo,
        )
    }

    fun toItem(parentItemListId: Long): Item {
        return Item(
            itemId = 0L,
            name = name,
            isChecked = false,
            category = category,
            quantity = quantity,
            unit = unit,
            memo = memo,
            parentItemListId = parentItemListId
        )
    }
}

@Fts4(contentEntity = DefaultItem::class)
@Entity
data class DefaultItemFts(
    val name: String,
    val category: String
)