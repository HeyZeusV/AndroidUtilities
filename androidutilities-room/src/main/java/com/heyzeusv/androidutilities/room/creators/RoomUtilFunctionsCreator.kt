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
import com.squareup.kotlinpoet.buildCodeBlock
import java.lang.UnsupportedOperationException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 *  Creates RoomUtilFunctions file, which contains functions that are used by multiple methods of
 *  export/import/backup/restore of Room database.
 */
internal class RoomUtilFunctionsCreator(
    private val codeGenerator: CodeGenerator,
    private val dbClassDeclaration: KSClassDeclaration,
    private val logger: KSPLogger,
) {
    private val uriClassName = ClassName("android.net", "Uri")
    private val documentFileClassName = ClassName("androidx.documentfile.provider", "DocumentFile")

    /**
     *  Creates RoomUtilFunctions.kt file.
     */
    private fun createRoomUtilFunctionsFile() {
        logger.info("Creating RoomUtilFunctions...")
        val packageName = dbClassDeclaration.getPackageName()
        val fileName = "RoomUtilFunctions"
        val fileBuilder = FileSpec.builder(packageName, fileName)

        fileBuilder.addFunction(buildCreateNewExportDirectoryFunction().build())
        fileBuilder.addFunction(buildFindOrCreateAppExportDirectoryFunction().build())

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dbClassDeclaration.containingFile!!),
            packageName = packageName,
            fileName = fileName,
            extensionName = "kt",
        ).bufferedWriter().use { fileBuilder.build().writeTo(it) }
    }

    /**
     *  Builds function that creates directory to place exported data using the date as a naming
     *  scheme.
     */
    private fun buildCreateNewExportDirectoryFunction(): FunSpec.Builder {
        val saveDirectory = "saveDirectory"
        val funSpec = FunSpec.builder("createNewExportDirectory")
            .returns(documentFileClassName.copy(nullable = true))
            .addParameter(saveDirectory, documentFileClassName)
            .addCode(buildCodeBlock {
                addStatement(
                    "val sdf = %T(%S, %T.getDefault())",
                    SimpleDateFormat::class, "MMM_dd_yyyy__hh_mm_aa", Locale::class,
                )
                addStatement("val formattedDate = sdf.format(%T())", Date::class)
                add("""
                    val newExportDirectory = $saveDirectory.createDirectory(formattedDate)
                    return newExportDirectory
                """.trimIndent())
            })

        return funSpec
    }

    /**
     *  Builds function that searches for appExportDirectoryName in selectedDirectoryUri and
     *  returns its Uri if found, else it creates appExportDirectoryName in selectedDirectoryUri.
     */
    private fun buildFindOrCreateAppExportDirectoryFunction(): FunSpec.Builder {
        val context = "context"
        val contextClassName = ClassName("android.content", "Context")

        val selectedDirectoryUri = "selectedDirectoryUri"
        val appExportDirectoryName = "appExportDirectoryName"

        val funSpec = FunSpec.builder("findOrCreateAppExportDirectory")
            .returns(uriClassName.copy(nullable = true))
            .addParameter(context, contextClassName)
            .addParameter(selectedDirectoryUri, uriClassName)
            .addParameter(appExportDirectoryName, String::class)
            .addCode(buildCodeBlock {
                addStatement("try {")
                addIndented {
                    addStatement(
                        "val selectedDirectory = %T.fromTreeUri(%L, %L)!!",
                        documentFileClassName, context, selectedDirectoryUri,
                    )
                    add("""
                        val appExportDirectory = selectedDirectory.findFile($appExportDirectoryName) ?:
                          selectedDirectory.createDirectory($appExportDirectoryName)!!
      
                        return appExportDirectory.uri
                    
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
        createRoomUtilFunctionsFile()
    }
}