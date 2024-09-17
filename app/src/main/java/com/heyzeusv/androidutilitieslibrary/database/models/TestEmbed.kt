package com.heyzeusv.androidutilitieslibrary.database.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import java.util.Date

data class TestEmbed(
    val test1: String = "",
    val test2: Int? = 0,
    val test3: Long? = 0L,
    @Embedded(prefix = "innerEmbed_")
    val embed: TestDoubleEmbed = TestDoubleEmbed()
)

data class TestDoubleEmbed(
    @ColumnInfo(name = "differentFieldName")
    val test3: String = "",
    val test5: Int = 0,
    val test6: Long = 0L,
    val test7: Date = Date(),
    val nullBoolean: Boolean? = null,
    val nullShort: Short? = null,
    val nullInt: Int? = null,
    val nullLong: Long? = null,
    val nullByte: Byte? = null,
    val nullString: String? = null,
    val nullChar: Char? = null,
    val nullDouble: Double? = null,
    val nullFloat: Float? = null,
    val nullByteArray: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestDoubleEmbed

        if (nullByteArray != null) {
            if (other.nullByteArray == null) return false
            if (!nullByteArray.contentEquals(other.nullByteArray)) return false
        } else if (other.nullByteArray != null) return false

        return true
    }

    override fun hashCode(): Int {
        return nullByteArray?.contentHashCode() ?: 0
    }
}