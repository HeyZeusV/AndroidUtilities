package com.heyzeusv.androidutilities.room.util

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import java.util.Locale

/**
 *  Returns the name of this class with suffix "RoomUtil" to differentiate between user created
 *  entity classes and entity classes created by this annotation processor.
 */
internal fun KSClassDeclaration.getUtilName(): String = "${simpleName.getShortName()}RoomUtil"

/**
 *  Returns the name of the package at which this class is declared as [String].
 */
internal fun KSClassDeclaration.getPackageName(): String = packageName.asString()

/**
 *  Adds [code] to this [CodeBlock.Builder] indented one level.
 */
internal fun CodeBlock.Builder.addIndented(code: CodeBlock.Builder.() -> Unit): CodeBlock.Builder =
    apply {
        indent()
        code()
        unindent()
    }

/**
 *  Indicates whether some other [TypeName] is "equal to" this if both were non-nullable.
 */
internal fun TypeName.equalsNullableType(type: TypeName): Boolean {
    return this.copy(nullable = false) == type.copy(nullable = false)
}

/**
 *  Returns [TypeName] of this if it was contained within a [List], i.e. List<this>
 */
internal fun TypeName.getListTypeName(): ParameterizedTypeName =
    ClassName("kotlin.collections", "List").parameterizedBy(this)

/**
 *  Checks if the specified [element], turned to non-null, is contained in this collection.
 */
internal fun List<TypeName>.containsNullableType(element: TypeName): Boolean {
    return contains(element.copy(false))
}

/**
 *  Returns the name of this class with lowercase first letter and suffix "Data" in order to be
 *  used as a parameter/property.
 */
internal fun ClassName.getDataName(): String {
    val lowercase = simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }
    return "${lowercase}Data"
}

/**
 *  Adds given [classDeclaration] as [ClassName] to this map as the key, with the value being
 *  given [classDeclaration] as [ClassName] with "RoomUtil" appended to the class name.
 */
internal fun MutableMap<ClassName, ClassName>.addOriginalAndUtil(
    classDeclaration: KSClassDeclaration,
) {
    val packageName = classDeclaration.getPackageName()
    val utilName = classDeclaration.getUtilName()
    this[classDeclaration.toClassName()] = ClassName(packageName, utilName)
}