package com.heyzeusv.androidutilitieslibrary.database.models

import androidx.room.Embedded

data class TestEmbed(
    val test1: String = "",
    val test2: Int = 0,
    val test3: Long = 0L,
    @Embedded
    val embed: TestDoubleEmbed = TestDoubleEmbed()
)

data class TestDoubleEmbed(
    val test3: String = "",
    val test5: Int = 0,
    val test6: Long = 0L,
)