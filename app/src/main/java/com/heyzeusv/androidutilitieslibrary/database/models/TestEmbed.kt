package com.heyzeusv.androidutilitieslibrary.database.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import java.util.Date

data class TestEmbed(
    val test1: String = "",
    val test2: Int = 0,
    val test3: Long = 0L,
    @Embedded(prefix = "innerEmbed_")
    val embed: TestDoubleEmbed = TestDoubleEmbed()
)

data class TestDoubleEmbed(
    @ColumnInfo(name = "differentFieldName")
    val test3: String = "",
    val test5: Int = 0,
    val test6: Long = 0L,
    val test7: Date = Date()
)