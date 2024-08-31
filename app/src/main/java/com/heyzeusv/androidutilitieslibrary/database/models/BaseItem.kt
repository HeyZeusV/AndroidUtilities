package com.heyzeusv.androidutilitieslibrary.database.models

interface BaseItem {
    val itemId: Long
    val name: String
    val category: String
    val quantity: Double
    val unit: String
    val memo: String

    fun editCopy(
        itemId: Long = this.itemId,
        name: String = this.name,
        category: String = this.category,
        quantity: Double = this.quantity,
        unit: String = this.unit,
        memo: String = this.memo
    ): BaseItem
}