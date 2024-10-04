package com.heyzeusv.androidutilities.room.creators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.util.Constants.EXTENSION_KT
import com.heyzeusv.androidutilities.room.util.Constants.ROOM_DATA
import com.heyzeusv.androidutilities.room.util.EntityInfo
import com.heyzeusv.androidutilities.room.util.addIndented
import com.heyzeusv.androidutilities.room.util.asListTypeName
import com.heyzeusv.androidutilities.room.util.getDataName
import com.heyzeusv.androidutilities.room.util.getPackageName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock

/**
 *  Creates RoomData file, which contains RoomData data class that contains lists of classes marked
 *  with Room.Entity. It is a helper class so that all entity data can be sent together to export or
 *  retrieved together when importing.
 *
 *  @param codeGenerator Creates file.
 *  @param dbClassDeclaration Class declaration annotated with Room.Database
 *  @param entityInfoList List of info on each Room.Entity annotated class.
 */
internal class RoomDataCreator(
    private val codeGenerator: CodeGenerator,
    private val dbClassDeclaration: KSClassDeclaration,
    private val entityInfoList: List<EntityInfo>,
    private val logger: KSPLogger,
) {
    private val csvDataClassName = ClassName(dbClassDeclaration.getPackageName(), "CsvData")
    private val csvInfoClassName = ClassName(dbClassDeclaration.getPackageName(), "CsvInfo")

    /**
     *  Creates RoomData.kt file.
     */
    private fun createRoomDataFile() {
        logger.info("Creating RoomData...")
        val packageName = dbClassDeclaration.getPackageName()
        val fileBuilder = FileSpec.builder(packageName, ROOM_DATA)
        val classBuilder = TypeSpec.classBuilder(ROOM_DATA)
            .addModifiers(KModifier.DATA)
            .buildRoomData()

        fileBuilder.addType(classBuilder.build())

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dbClassDeclaration.containingFile!!),
            packageName = packageName,
            fileName = ROOM_DATA,
            extensionName = EXTENSION_KT,
        ).bufferedWriter().use { fileBuilder.build().writeTo(it) }
    }

    /**
     *  RoomData is pretty simple, just constructor and a few additional properties, so entire class
     *  is built here using [entityInfoList].
     */
    private fun TypeSpec.Builder.buildRoomData(): TypeSpec.Builder {
        val constructorBuilder = FunSpec.constructorBuilder()
        val csvDataMapCodeBlockBuilder = CodeBlock.builder().addStatement("mapOf(").indent()

        entityInfoList.forEach { entityInfo ->
            // parameter/property list of user created entity
            val originalDataName = entityInfo.originalClassName.getDataName()
            val originalParameterBuilder = ParameterSpec
                .builder(originalDataName, entityInfo.originalClassName.asListTypeName())
                .defaultValue("emptyList()")
            val originalPropertyBuilder = PropertySpec
                .builder(originalDataName, entityInfo.originalClassName.asListTypeName())
                .initializer(originalDataName)
            constructorBuilder.addParameter(originalParameterBuilder.build())
            addProperty(originalPropertyBuilder.build())

            // property list of RoomUtil created by EntityFilesCreator
            val utilDataName = entityInfo.utilClassName.getDataName()
            val utilPropertyBuilder = PropertySpec
                .builder(utilDataName, entityInfo.utilClassName.asListTypeName())
                .addModifiers(KModifier.PRIVATE)
                .initializer(
                    buildCodeBlock {
                        addStatement("%L.map {", originalDataName)
                        addIndented {
                            addStatement("%L.toUtil(it)", entityInfo.utilClassName.simpleName)
                        }
                        add("}")
                    }
                )
            addProperty(utilPropertyBuilder.build())

            // property map of *RoomUtil class to list of *RoomUtil created above.
            csvDataMapCodeBlockBuilder.addStatement(
                "%L to %L,",
                entityInfo.utilClassName.simpleName, entityInfo.utilClassName.getDataName(),
            )
        }
        
        csvDataMapCodeBlockBuilder.unindent().add(")")
        val csvMapClassName = ClassName("kotlin.collections", "Map")
            .parameterizedBy(csvInfoClassName, csvDataClassName.asListTypeName())
        val csvDataMapPropertyBuilder = PropertySpec.builder("csvDataMap", csvMapClassName)
            .initializer(csvDataMapCodeBlockBuilder.build())
        
        primaryConstructor(constructorBuilder.build())
        addProperty(csvDataMapPropertyBuilder.build())

        return this
    }
    
    init {
        createRoomDataFile()
    }
}