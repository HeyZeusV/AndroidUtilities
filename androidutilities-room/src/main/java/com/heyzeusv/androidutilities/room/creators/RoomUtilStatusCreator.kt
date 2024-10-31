package com.heyzeusv.androidutilities.room.creators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.util.Constants.EXTENSION_KT
import com.heyzeusv.androidutilities.room.util.Constants.ROOM_UTIL_STATUS
import com.heyzeusv.androidutilities.room.util.getPackageName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

internal class RoomUtilStatusCreator(
    private val codeGenerator: CodeGenerator,
    private val dbClassDeclaration: KSClassDeclaration,
    private val logger: KSPLogger,
) {
    private val packageName = dbClassDeclaration.getPackageName()

    private fun createRoomUtilStatusFile() {
        logger.info("Creating RoomUtilStatus")
        val fileBuilder = FileSpec.builder(packageName, ROOM_UTIL_STATUS)

        val classBuilder = TypeSpec.classBuilder(ROOM_UTIL_STATUS)

        fileBuilder.addType(classBuilder.build())

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dbClassDeclaration.containingFile!!),
            packageName = packageName,
            fileName = ROOM_UTIL_STATUS,
            extensionName = EXTENSION_KT,
        ).bufferedWriter().use { fileBuilder.build().writeTo(it) }
    }

    init {
        createRoomUtilStatusFile()
    }
}