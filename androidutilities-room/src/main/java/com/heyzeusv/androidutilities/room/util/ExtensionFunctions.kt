package com.heyzeusv.androidutilities.room.util

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import java.util.Locale
import kotlin.reflect.KClass

/**
 *  Returns the name of this declaration.
 */
internal fun KSDeclaration.getName(): String = simpleName.getShortName()

/**
 *  Returns the name of the package at which this declaration is declared at as [String].
 */
internal fun KSDeclaration.getPackageName(): String = packageName.asString()

/**
 *  Returns the name of this class with suffix "RoomUtil" to differentiate between user created
 *  entity classes and entity classes created by this annotation processor.
 */
internal fun KSClassDeclaration.getUtilName(): String = "${getName()}RoomUtil"

/**
 *  Returns the value of [argument] belonging to [annotation] in the form of a [String]. Assumes
 *  that [annotation] and [argument] 100% exists.
 */
internal fun KSAnnotated.getAnnotationArgumentValue(
    annotation: String,
    argument: String,
): String =
    annotations.getWithName(annotation).arguments.getWithName(argument).value.toString()

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

// TODO: check if all usages can be replaced with asListTypeName() below
/**
 *  Returns [TypeName] of this if it was contained within a [List], i.e. List<this>
 */
internal fun TypeName.getListTypeName(): ParameterizedTypeName =
    ClassName("kotlin.collections", "List").parameterizedBy(this)

/**
 *  Returns this [TypeName] as [String] with "kotlin." prefix removed.
 */
internal fun TypeName.removeKotlinPrefix(): String = toString().removePrefix("kotlin.")

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
 *  Returns [KSAnnotation] with given [annotationName], assumes that given [annotationName] 100%
 *  exists in this sequence.
 */
internal fun Sequence<KSAnnotation>.getWithName(annotationName: String): KSAnnotation =
    find { it.shortName.getShortName() == annotationName }!!

/**
 *  Returns [KSValueArgument] with given [argumentName], assumes that given [argumentName] 100%
 *  exists in this list.
 */
internal fun List<KSValueArgument>.getWithName(argumentName: String): KSValueArgument =
    find { it.name!!.getShortName() == argumentName }!!

/**
 *  If this [String] is not blank, return this [String] with [value] appended onto the end else
 *  return blank [String].
 */
internal fun String.ifNotBlankAppend(value: String): String {
    return if (isNotBlank()) this + value else this
}

/**
 *  Returns [TypeName] of this if it was contained within a [List], i.e. List<*>
 */
internal fun KClass<*>.asListTypeName(): ParameterizedTypeName =
    ClassName("kotlin.collections", "List").parameterizedBy(this.asTypeName())