package com.heyzeusv.androidutilities.room.creators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.util.Constants.APP_DIRECTORY_NAME
import com.heyzeusv.androidutilities.room.util.Constants.CONTEXT
import com.heyzeusv.androidutilities.room.util.Constants.EXTENSION_KT
import com.heyzeusv.androidutilities.room.util.Constants.ROOM_UTIL_BASE
import com.heyzeusv.androidutilities.room.util.Constants.ROOM_UTIL_STATUS
import com.heyzeusv.androidutilities.room.util.Constants.SELECTED_DIRECTORY_URI
import com.heyzeusv.androidutilities.room.util.Constants.STATUS_STANDBY
import com.heyzeusv.androidutilities.room.util.Constants.asStateFlowClassName
import com.heyzeusv.androidutilities.room.util.Constants.contextClassName
import com.heyzeusv.androidutilities.room.util.Constants.documentFileClassName
import com.heyzeusv.androidutilities.room.util.Constants.mutableStateFlowClassName
import com.heyzeusv.androidutilities.room.util.Constants.stateFlowClassName
import com.heyzeusv.androidutilities.room.util.Constants.uriClassName
import com.heyzeusv.androidutilities.room.util.getPackageName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import java.lang.UnsupportedOperationException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 *  Creates RoomUtilBase file, which contains functions that are used by multiple methods of
 *  export/import/backup/restore of Room database.
 */
internal class RoomUtilBaseCreator(
    private val codeGenerator: CodeGenerator,
    private val dbClassDeclaration: KSClassDeclaration,
    private val logger: KSPLogger,
) {
    private val packageName = dbClassDeclaration.getPackageName()

    private val statusClassName = ClassName(packageName, ROOM_UTIL_STATUS)
    private val statusStandbyClassName = ClassName("$packageName.$ROOM_UTIL_STATUS", STATUS_STANDBY)

    /**
     *  Creates RoomUtilBase.kt file.
     */
    private fun createRoomUtilBaseFile() {
        logger.info("Creating RoomUtilBase...")
        val packageName = dbClassDeclaration.getPackageName()
        val fileBuilder = FileSpec.builder(packageName, ROOM_UTIL_BASE)
        val classBuilder = TypeSpec.classBuilder(ROOM_UTIL_BASE)
            .buildRoomUtilBase()

        fileBuilder.addType(classBuilder.build())

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dbClassDeclaration.containingFile!!),
            packageName = packageName,
            fileName = ROOM_UTIL_BASE,
            extensionName = EXTENSION_KT,
        ).bufferedWriter().use { fileBuilder.build().writeTo(it) }
    }

    /**
     *  Builds RoomUtilBase TypeSpec by adding parameters/properties and functions.
     */
    private fun TypeSpec.Builder.buildRoomUtilBase(): TypeSpec.Builder {
        addModifiers(KModifier.ABSTRACT)

        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(CONTEXT, contextClassName)
                .addParameter(APP_DIRECTORY_NAME, String::class)
                .build()
        )
        addProperty(
            PropertySpec.builder(CONTEXT, contextClassName)
                .initializer(CONTEXT)
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        addProperty(
            PropertySpec.builder(APP_DIRECTORY_NAME, String::class)
                .initializer(APP_DIRECTORY_NAME)
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        addProperty(
            PropertySpec.builder("_status", mutableStateFlowClassName.parameterizedBy(statusClassName))
                .initializer(buildCodeBlock { add("MutableStateFlow(%T)", statusStandbyClassName) })
                .addModifiers(KModifier.PROTECTED)
                .build()
        )
        addProperty(
            PropertySpec.builder("status", stateFlowClassName.parameterizedBy(statusClassName))
                .initializer(buildCodeBlock { add("_status.%T()", asStateFlowClassName) })
                .build()
        )

        addFunction(buildUpdateStatusFunction().build())
        addFunction(buildCreateNewDirectoryFunction().build())
        addFunction(buildFindOrCreateAppDirectoryFunction().build())

        return this
    }

    /**
     *  Builds function to update status value.
     */
    private fun buildUpdateStatusFunction(): FunSpec.Builder {
        val funSpec = FunSpec.builder("updateStatus")
            .addParameter("newValue", statusClassName)
            .addCode(buildCodeBlock {
                addStatement("_status.value = newValue")
            })

        return funSpec
    }

    /**
     *  Builds function that creates directory to place exported data using the date as a naming
     *  scheme.
     */
    private fun buildCreateNewDirectoryFunction(): FunSpec.Builder {
        val funSpec = FunSpec.builder("createNewDirectory")
            .addModifiers(KModifier.PROTECTED)
            .returns(documentFileClassName.copy(nullable = true))
            .addParameter("appDirectory", documentFileClassName)
            .addCode(buildCodeBlock {
                add("""
                    val sdf = %T(%S, %T.getDefault())
                    val formattedDate = sdf.format(%T())
                    val newDirectory = appDirectory.createDirectory(formattedDate)
                    return newDirectory
                """.trimIndent(),
                    SimpleDateFormat::class, "MMM_dd_yyyy__hh_mm_aa", Locale::class, Date::class
                )
            })

        return funSpec
    }

    /**
     *  Builds function that searches for appExportDirectoryName in selectedDirectoryUri and
     *  returns its Uri if found, else it creates appExportDirectoryName in selectedDirectoryUri.
     */
    private fun buildFindOrCreateAppDirectoryFunction(): FunSpec.Builder {
        val funSpec = FunSpec.builder("findOrCreateAppDirectory")
            .returns(uriClassName.copy(nullable = true))
            .addParameter(SELECTED_DIRECTORY_URI, uriClassName)
            .addCode(buildCodeBlock {
                add("""
                    try {
                      val selectedDirectory = %T.fromTreeUri(context, selectedDirectoryUri)!!
                      val appDirectory = selectedDirectory.findFile(appDirectoryName) ?:
                        selectedDirectory.createDirectory(appDirectoryName)!!
                        
                      return appDirectory.uri
                    } catch (e: %T) {
                      // Don't use fromSingleUri(Context, Uri)
                      return null
                    }
                """.trimIndent(), documentFileClassName, UnsupportedOperationException::class)
            })

        return funSpec
    }

    init {
        createRoomUtilBaseFile()
    }
}