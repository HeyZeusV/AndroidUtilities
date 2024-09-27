package com.heyzeusv.androidutilitieslibrary.database.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "an_item_table_name",
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
data class Item(
    @PrimaryKey(autoGenerate = true)
    var itemId: Long = 0L,
    var name: String = "",
    var category: String = "",
    var quantity: Double = 0.0,
    var unit: String = "",
    var memo: String = "",
    @Embedded(prefix = "outerEmbed_")
    var outerEmbed: SampleOuterEmbed = SampleOuterEmbed(),
    @Ignore
    var ignoreField: String = "",
)

@Fts4(contentEntity = Item::class)
@Entity
data class ItemFts(
    val name: String,
    val category: String
)