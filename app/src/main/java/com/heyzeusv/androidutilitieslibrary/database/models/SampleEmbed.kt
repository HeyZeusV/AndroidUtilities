package com.heyzeusv.androidutilitieslibrary.database.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Ignore
import java.util.Date

data class SampleOuterEmbed(
    var sameName: String = "",
    var someField: Int? = 0,
    var uselessField: Long? = 0L,
    @Ignore
    var ignoreField: String = "ignoreMe",
    @Embedded(prefix = "innerEmbed_")
    var embed: SampleInnerEmbed = SampleInnerEmbed()
)

data class SampleInnerEmbed(
    @ColumnInfo(name = "differentFieldName")
    val sameName: String = "",
    val typeConverterField: Date = Date(),
    val nullableBoolean: Boolean? = null,
    val nullableShort: Short? = null,
    val nullableInt: Int? = null,
    val nullableLong: Long? = null,
    val nullableByte: Byte? = null,
    val nullableString: String? = null,
    val nullableChar: Char? = null,
    val nullableDouble: Double? = null,
    val nullableFloat: Float? = null,
    val nullableByteArray: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SampleInnerEmbed

        if (nullableByteArray != null) {
            if (other.nullableByteArray == null) return false
            if (!nullableByteArray.contentEquals(other.nullableByteArray)) return false
        } else if (other.nullableByteArray != null) return false

        return true
    }

    override fun hashCode(): Int {
        return nullableByteArray?.contentHashCode() ?: 0
    }
}