package com.heyzeusv.androidutilities.room.creators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.util.Constants.EXTENSION_KT
import com.heyzeusv.androidutilities.room.util.Constants.MESSAGE_ID
import com.heyzeusv.androidutilities.room.util.Constants.NAME
import com.heyzeusv.androidutilities.room.util.Constants.ROOM_UTIL_STATUS
import com.heyzeusv.androidutilities.room.util.Constants.STATUS_ERROR
import com.heyzeusv.androidutilities.room.util.Constants.STATUS_PROGRESS
import com.heyzeusv.androidutilities.room.util.Constants.STATUS_STANDBY
import com.heyzeusv.androidutilities.room.util.Constants.STATUS_SUCCESS
import com.heyzeusv.androidutilities.room.util.Constants.stringResClassName
import com.heyzeusv.androidutilities.room.util.getPackageName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal class RoomUtilStatusCreator(
    private val codeGenerator: CodeGenerator,
    private val dbClassDeclaration: KSClassDeclaration,
    private val logger: KSPLogger,
) {
    private val packageName = dbClassDeclaration.getPackageName()
    private val statusClassName = ClassName(packageName, ROOM_UTIL_STATUS)

    private fun createRoomUtilStatusFile() {
        logger.info("Creating RoomUtilStatus")
        val fileBuilder = FileSpec.builder(packageName, ROOM_UTIL_STATUS)

        val classBuilder = TypeSpec.classBuilder(ROOM_UTIL_STATUS)
            .buildRoomUtilStatus()

        fileBuilder.addType(classBuilder.build())

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dbClassDeclaration.containingFile!!),
            packageName = packageName,
            fileName = ROOM_UTIL_STATUS,
            extensionName = EXTENSION_KT,
        ).bufferedWriter().use { fileBuilder.build().writeTo(it) }
    }

    private fun TypeSpec.Builder.buildRoomUtilStatus(): TypeSpec.Builder {
        addModifiers(KModifier.SEALED)

        val standbyBuilder = TypeSpec.objectBuilder(STATUS_STANDBY)
            .addModifiers(KModifier.DATA)
            .superclass(statusClassName)
        addType(standbyBuilder.build())

        val progressBuilder = buildProgressErrorClasses(STATUS_PROGRESS)
        addType(progressBuilder.build())

        val errorBuilder = buildProgressErrorClasses(STATUS_ERROR)
        addType(errorBuilder.build())

        val successBuilder = buildSuccessClass()
        addType(successBuilder.build())

        return this
    }

    private fun buildProgressErrorClasses(name: String): TypeSpec.Builder {
        val messageParameterBuilder = ParameterSpec.builder(MESSAGE_ID, Int::class)
            .addAnnotation(stringResClassName)
        val nameParameterBuilder = ParameterSpec.builder(NAME, String::class)
            .defaultValue("\"\"")
        val constructorBuilder = FunSpec.constructorBuilder()
            .addParameter(messageParameterBuilder.build())
            .addParameter(nameParameterBuilder.build())
        val classBuilder = TypeSpec.classBuilder(name)
            .addModifiers(KModifier.DATA)
            .superclass(statusClassName)
            .primaryConstructor(constructorBuilder.build())
            .addProperty(PropertySpec.builder(MESSAGE_ID, Int::class)
                .initializer(MESSAGE_ID)
                .build()
            )
            .addProperty(PropertySpec.builder(NAME, String::class)
                .initializer(NAME)
                .build()
            )

        return classBuilder
    }

    private fun buildSuccessClass(): TypeSpec.Builder {
        val messageParameterBuilder = ParameterSpec.builder(MESSAGE_ID, Int::class)
            .addAnnotation(stringResClassName)
        val constructorBuilder = FunSpec.constructorBuilder()
            .addParameter(messageParameterBuilder.build())
        val classBuilder = TypeSpec.classBuilder(STATUS_SUCCESS)
            .addModifiers(KModifier.DATA)
            .superclass(statusClassName)
            .primaryConstructor(constructorBuilder.build())
            .addProperty(PropertySpec.builder(MESSAGE_ID, Int::class)
                .initializer(MESSAGE_ID)
                .build()
            )

        return classBuilder
    }

    init {
        createRoomUtilStatusFile()
    }
}