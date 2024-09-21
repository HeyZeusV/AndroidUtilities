package com.heyzeusv.androidutilities.room.util

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.TypeName

/**
 *  Information on properties found in Room.Entity annotated class.
 *
 *  @property name The name of property.
 */
interface PropertyInfo {
    val name: String
}

/**
 *  All properties that are not annotated with Room.Embedded.
 *
 *  @param name The name of property.
 *  @param fieldName The name of property when saved to Room, could be the same value as [name] or
 *  be updated using Room.ColumnInfo.name and Room.Embedded.prefix.
 *  @param androidType The type of property when created in Android environment.
 *  @param roomType The type of property when saved to Room/SQLite database.
 *
 */
data class FieldInfo(
    override val name: String,
    val fieldName: String,
    val androidType: TypeName,
    val roomType: TypeName,
) : PropertyInfo

/**
 *  All properties that are annotated with Room.Embedded.
 *
 *  @param name The name of property.
 *  @param embeddedClass
 */
data class EmbeddedInfo(
    override val name: String,
    val embeddedClass: KSClassDeclaration,
) : PropertyInfo

/**
 *  Used to determine when all properties of an Embedded class have been recorded, so that
 *  KotlinPoet can be used to close Embedded class.
 */
data class CloseClass(override val name: String = "") : PropertyInfo
