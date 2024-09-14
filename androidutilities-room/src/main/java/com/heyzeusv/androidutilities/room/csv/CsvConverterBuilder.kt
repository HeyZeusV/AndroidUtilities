package com.heyzeusv.androidutilities.room.csv

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val CONTEXT_PROP = "context"
private val contextClassName = ClassName("android.content", "Context")
private val uriClassName = ClassName("android.net", "Uri")
private val documentFileClassName = ClassName("androidx.documentfile.provider", "DocumentFile")
private val csvDataListClassName = ClassName("kotlin.collections", "List")
    .parameterizedBy(CsvData::class.asTypeName())

internal fun buildCsvConverter(
    codeGenerator: CodeGenerator,
    dbClass: KSClassDeclaration,
    logger: KSPLogger,
) {
    val packageName = dbClass.containingFile?.packageName?.asString().orEmpty()
    val fileName = "CsvConverter"

    val fileSpecBuilder = FileSpec.builder("$packageName.csv", fileName)

    val classBuilder = TypeSpec
        .classBuilder(fileName)
        .primaryConstructor(FunSpec.constructorBuilder()
            .addParameter(CONTEXT_PROP, contextClassName)
            .build()
        )
        .addProperty(PropertySpec.builder(CONTEXT_PROP, contextClassName)
            .initializer(CONTEXT_PROP)
            .addModifiers(KModifier.PRIVATE)
            .build()
        )

    classBuilder.addFunction(exportRoomToCsvFunSpec().build())
    classBuilder.addFunction(exportRoomEntityToCsvFunSpec().build())
    classBuilder.addFunction(createNewExportDirectoryFunSpec().build())
    classBuilder.addFunction(findOrCreateParentDirectoryFunSpec().build())
    fileSpecBuilder.addType(classBuilder.build())

    codeGenerator.createNewFile(
        dependencies = Dependencies(false, dbClass.containingFile!!),
        packageName = packageName,
        fileName = fileName,
        extensionName = "kt",
    ).bufferedWriter().use { fileSpecBuilder.build().writeTo(it) }
}

private fun exportRoomToCsvFunSpec(): FunSpec.Builder {
    val parentDirectoryUri = "parentDirectoryUri"
    val dataMap = "dataMap"
    val csvMapClassName = ClassName("kotlin.collections", "Map")
        .parameterizedBy(CsvInfo::class.asTypeName(), csvDataListClassName)
    val funSpec = FunSpec.builder("exportRoomToCsv")
        .addParameter(parentDirectoryUri, uriClassName)
        .addParameter(dataMap, csvMapClassName)
        .addCode(buildCodeBlock {
            addStatement("val parentDirectory = %T.fromTreeUri(%L, $parentDirectoryUri)!!", documentFileClassName, CONTEXT_PROP)
            add("""
                if (!parentDirectory.exists()) {
                  // given directory doesn't exist
                  return
                } else {
                  val newExportDirectory = createNewExportDirectory(parentDirectory)
                  if (newExportDirectory == null) {
                    // failed to create directory
                    return
                  } else {
                    val newCsvDocumentFiles = mutableListOf<DocumentFile>()
                    $dataMap.entries.forEach {
                    val csvDocumentFile = exportRoomEntityToCsv(
                      newExportDirectory = newExportDirectory,
                      csvInfo = it.key,
                      csvDataList = it.value,
                      )
                      newCsvDocumentFiles.add(csvDocumentFile)
                    }
                  }
                }
            """.trimIndent())
        })
    return funSpec
}

private fun exportRoomEntityToCsvFunSpec(): FunSpec.Builder {
    val newExportDirectory = "newExportDirectory"
    val csvWriterMemberName = MemberName("com.github.doyaaaaaken.kotlincsv.dsl", "csvWriter")
    val funSpec = FunSpec.builder("exportRoomEntityToCsv")
        .addModifiers(KModifier.PRIVATE)
        .returns(documentFileClassName)
        .addParameter(newExportDirectory, documentFileClassName)
        .addParameter("csvInfo", CsvInfo::class)
        .addParameter("csvDataList", csvDataListClassName)
        .addCode(buildCodeBlock {
            addStatement("val csvDocumentFile = $newExportDirectory.createFile(%S, csvInfo.csvFileName)!!", "text/*")
            addStatement("val outputStream = %L.contentResolver.openOutputStream(csvDocumentFile.uri)!!", CONTEXT_PROP)
            addStatement("%M().open(outputStream) {", csvWriterMemberName)
            add("""
                  writeRow(csvInfo.csvHeader)
                  csvDataList.forEach { writeRow(it.csvRow) }
                }
                return csvDocumentFile
            """.trimIndent())
        })

    return funSpec
}

private fun createNewExportDirectoryFunSpec(): FunSpec.Builder {
    val parentDirectory = "parentDirectory"
    val funSpec = FunSpec.builder("createNewExportDirectory")
        .addModifiers(KModifier.PRIVATE)
        .returns(documentFileClassName.copy(nullable = true))
        .addParameter(parentDirectory, documentFileClassName)
        .addCode(buildCodeBlock {
            addStatement("val sdf = %T(%S, %T.getDefault())", SimpleDateFormat::class, "MMMM_dd_yyyy__hh_mm_aa", Locale::class)
            addStatement("val formattedDate = sdf.format(%T())", Date::class)
            add("""
                val newExportDirectory = $parentDirectory.createDirectory(formattedDate)
                return newExportDirectory
            """.trimIndent())
        })

    return funSpec
}

private fun findOrCreateParentDirectoryFunSpec(): FunSpec.Builder {
    val parentDirectoryName = "parentDirectoryName"
    val selectedDirectoryUri = "selectedDirectoryUri"
    val funSpec = FunSpec.builder("findOrCreateParentDirectory")
        .returns(uriClassName.copy(nullable = true))
        .addParameter(parentDirectoryName, String::class)
        .addParameter(selectedDirectoryUri, uriClassName)
        .addCode(buildCodeBlock {
            addStatement("try {")
            addIndented {
                addStatement("val selectedDirectory = %T.fromTreeUri(%L, %L)!!", documentFileClassName, CONTEXT_PROP, selectedDirectoryUri)
                add("""
                    var parentDirectory = selectedDirectory.findFile($parentDirectoryName)
                    if (parentDirectory == null) {
                     parentDirectory = selectedDirectory.createDirectory($parentDirectoryName)!!
                    }
                    return parentDirectory.uri
                    
                """.trimIndent())
            }
            addStatement("} catch (e: %T) {", Exception::class)
            addIndented { addStatement("return null") }
            addStatement("}")
        })

    return funSpec
}

fun CodeBlock.Builder.addIndented(code: CodeBlock.Builder.() -> Unit): CodeBlock.Builder = apply {
    indent()
    code()
    unindent()
}