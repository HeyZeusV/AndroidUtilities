package com.heyzeusv.androidutilities.room.creators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.util.addIndented
import com.heyzeusv.androidutilities.room.util.getPackageName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
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
    private val contextClassName = ClassName("android.content", "Context")
    private val uriClassName = ClassName("android.net", "Uri")
    private val documentFileClassName = ClassName("androidx.documentfile.provider", "DocumentFile")

    /**
     *  Creates RoomUtilBase.kt file.
     */
    private fun createRoomUtilBaseFile() {
        logger.info("Creating RoomUtilBase...")
        val packageName = dbClassDeclaration.getPackageName()
        val fileName = "RoomUtilBase"
        val fileBuilder = FileSpec.builder(packageName, fileName)
        val classBuilder = TypeSpec.classBuilder(fileName)
            .buildRoomUtilBase()

        fileBuilder.addType(classBuilder.build())

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dbClassDeclaration.containingFile!!),
            packageName = packageName,
            fileName = fileName,
            extensionName = "kt",
        ).bufferedWriter().use { fileBuilder.build().writeTo(it) }
    }

    /**
     *  Builds RoomUtilBase TypeSpec by adding parameters/properties and functions.
     */
    private fun TypeSpec.Builder.buildRoomUtilBase(): TypeSpec.Builder {
        addModifiers(KModifier.ABSTRACT)

        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("context", contextClassName)
                .addParameter("appDirectoryName", String::class)
                .build()
        )
        addProperty(
            PropertySpec.builder("context", contextClassName)
                .initializer("context")
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        addProperty(
            PropertySpec.builder("appDirectoryName", String::class)
                .initializer("appDirectoryName")
                .addModifiers(KModifier.PRIVATE)
                .build()
        )

        addFunction(buildCreateNewDirectoryFunction().build())
        addFunction(buildFindOrCreateAppDirectoryFunction().build())

        return this
    }

    /**
     *  Builds function that creates directory to place exported data using the date as a naming
     *  scheme.
     */
    private fun buildCreateNewDirectoryFunction(): FunSpec.Builder {
        val appDirectory = "appDirectory"
        val funSpec = FunSpec.builder("createNewDirectory")
            .addModifiers(KModifier.PROTECTED)
            .returns(documentFileClassName.copy(nullable = true))
            .addParameter(appDirectory, documentFileClassName)
            .addCode(buildCodeBlock {
                addStatement(
                    "val sdf = %T(%S, %T.getDefault())",
                    SimpleDateFormat::class, "MMM_dd_yyyy__hh_mm_aa", Locale::class,
                )
                addStatement("val formattedDate = sdf.format(%T())", Date::class)
                add("""
                    val newDirectory = $appDirectory.createDirectory(formattedDate)
                    return newDirectory
                """.trimIndent())
            })

        return funSpec
    }

    /**
     *  Builds function that searches for appExportDirectoryName in selectedDirectoryUri and
     *  returns its Uri if found, else it creates appExportDirectoryName in selectedDirectoryUri.
     */
    private fun buildFindOrCreateAppDirectoryFunction(): FunSpec.Builder {
        val context = "context"

        val selectedDirectoryUri = "selectedDirectoryUri"
        val appDirectoryName = "appDirectoryName"

        val funSpec = FunSpec.builder("findOrCreateAppDirectory")
            .returns(uriClassName.copy(nullable = true))
            .addParameter(selectedDirectoryUri, uriClassName)
            .addCode(buildCodeBlock {
                addStatement("try {")
                addIndented {
                    addStatement(
                        "val selectedDirectory = %T.fromTreeUri(%L, %L)!!",
                        documentFileClassName, context, selectedDirectoryUri,
                    )
                    add("""
                        val appDirectory = selectedDirectory.findFile($appDirectoryName) ?:
                          selectedDirectory.createDirectory($appDirectoryName)!!
      
                        return appDirectory.uri
                    
                    """.trimIndent())
                }
                addStatement("} catch (e: %T) {", UnsupportedOperationException::class)
                addIndented {
                    addStatement("// Don't use fromSingleUri(Context, Uri)")
                    addStatement("return null")
                }
                addStatement("}")
            })

        return funSpec
    }

    init {
        createRoomUtilBaseFile()
    }
}