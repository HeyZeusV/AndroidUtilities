package com.heyzeusv.androidutilities.room

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.heyzeusv.androidutilities.room.util.getName
import com.heyzeusv.androidutilities.room.util.getPackageName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 *  Contains all the information needed by KotlinPoet to create coke that can call a function
 *  annotated with Room.TypeConverter.
 *
 *  [packageName] and [className] are used as parameters to create a [ClassName]. This [ClassName]
 *  is used to automatically import [className] using KotlinPoet's %T format specifier. The
 *  function can then be called using [functionName]. [parameterType] and [returnType] are used
 *  to determine which TypeConverter function to use. This can be seen at [handlePropertyInfoToUtil]
 *  under is FieldInfo condition.
 *
 *  @param packageName Name of package where TypeConverter is declared at.
 *  @param className Name of class that TypeConverter belongs to.
 *  @param functionName Name of TypeConverter function.
 *  @param parameterType TypeConverter's parameter type.
 *  @param returnType TypeConverter's return type.
 */
data class TypeConverterInfo(
    val packageName: String,
    val className: String,
    val functionName: String,
    val parameterType: TypeName,
    val returnType: TypeName,
) {
    companion object {
        /**
         *  Creates a [TypeConverterInfo] using given [KSFunctionDeclaration].
         *
         *  @param funDeclaration Contains all the information needed to create [TypeConverterInfo].
         */
        fun fromFunctionDeclaration(funDeclaration: KSFunctionDeclaration): TypeConverterInfo =
            TypeConverterInfo(
                packageName = funDeclaration.getPackageName(),
                className = funDeclaration.parentDeclaration!!.getName(),
                functionName = funDeclaration.getName(),
                parameterType = funDeclaration.parameters.first().type.toTypeName(),
                returnType = funDeclaration.returnType!!.toTypeName(),
            )
    }
}