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

        fileBuilder.addFunction(buildCreateNewDirectoryFunction().build())
        fileBuilder.addFunction(buildFindOrCreateAppDirectoryFunction().build())

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
    private fun buildCreateNewDirectoryFunction(): FunSpec.Builder {
        val appDirectory = "appDirectory"
        val funSpec = FunSpec.builder("createNewDirectory")
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
        val contextClassName = ClassName("android.content", "Context")

        val selectedDirectoryUri = "selectedDirectoryUri"
        val appDirectoryName = "appDirectoryName"

        val funSpec = FunSpec.builder("findOrCreateAppDirectory")
            .returns(uriClassName.copy(nullable = true))
            .addParameter(context, contextClassName)
            .addParameter(selectedDirectoryUri, uriClassName)
            .addParameter(appDirectoryName, String::class)
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
        createRoomUtilFunctionsFile()
    }
}