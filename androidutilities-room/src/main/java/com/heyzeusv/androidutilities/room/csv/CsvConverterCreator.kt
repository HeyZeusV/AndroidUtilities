package com.heyzeusv.androidutilities.room.csv

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.EntityData
import com.heyzeusv.androidutilities.room.util.addIndented
import com.heyzeusv.androidutilities.room.util.asListTypeName
import com.heyzeusv.androidutilities.room.util.getDataName
import com.heyzeusv.androidutilities.room.util.getListTypeName
import com.heyzeusv.androidutilities.room.util.getPackageName
import com.heyzeusv.androidutilities.room.util.removeKotlinPrefix
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val FILE_NAME = "CsvConverter"
private const val CONTEXT_PROP = "context"

internal class CsvConverterCreator(
    private val codeGenerator: CodeGenerator,
    private val dbClassDeclaration: KSClassDeclaration,
    private val entityDataList: List<EntityData>,
    private val logger: KSPLogger,
) {
    private val packageName = dbClassDeclaration.getPackageName()

    private val roomDataClassName = ClassName(packageName, "RoomData")
    private val uriClassName = ClassName("android.net", "Uri")
    private val documentFileClassName = ClassName("androidx.documentfile.provider", "DocumentFile")
    private val csvDataListClassName = CsvData::class.asListTypeName()

    private fun createCsvConverterFile() {
        logger.info("Creating CsvConverter...")
        val fileBuilder = FileSpec.builder(packageName, FILE_NAME)

        val classBuilder = TypeSpec.classBuilder(FILE_NAME)
            .buildCsvConverter()

        fileBuilder.addType(classBuilder.build())

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dbClassDeclaration.containingFile!!),
            packageName = packageName,
            fileName = FILE_NAME,
            extensionName = "kt"
        ).bufferedWriter().use { fileBuilder.build().writeTo(it) }
    }

    /**
     *  Builds CsvConverter TypeSpec by adding parameters/properties and functions.
     */
    private fun TypeSpec.Builder.buildCsvConverter(): TypeSpec.Builder {
        val contextClassName = ClassName("android.content", "Context")
        // context parameter/property in order to read/write files
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(CONTEXT_PROP, contextClassName)
                .build()
        )
        addProperty(
            PropertySpec.builder(CONTEXT_PROP, contextClassName)
                .initializer(CONTEXT_PROP)
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        addProperty(
            PropertySpec.builder("csvFileNames", String::class.asTypeName().getListTypeName())
                .initializer(buildCodeBlock {
                    addStatement("listOf(")
                    addIndented {
                        val tableNames = entityDataList.map { it.tableName }
                        tableNames.forEachIndexed { index, tableName ->
                            add("%S", "$tableName.csv")
                            if (index < tableNames.size) add(", ")
                        }
                        addStatement("")
                    }
                    add(")")
                })
                .addModifiers(KModifier.PRIVATE)
                .build()
        )

        addFunction(buildImportCsvToRoomFunction().build())
        addFunction(buildImportCsvToRoomEntityFunction().build())
        addFunction(buildExportRoomToCsvFunction().build())
        addFunction(buildExportRoomEntityToCsv().build())
        addFunction(buildCreateNewExportDirectoryFunction().build())
        addFunction(buildFindOrCreateSaveDirectoryFunction().build())

        return this
    }

    private fun buildImportCsvToRoomFunction(): FunSpec.Builder {
        val selectedDirectoryUri = "selectedDirectoryUri"

        val funSpec = FunSpec.builder("importCsvToRoom")
            .addAnnotation(
                AnnotationSpec.builder(Suppress::class)
                .addMember("%S", "UNCHECKED_CAST")
                .build()
            )
            .addParameter(selectedDirectoryUri, uriClassName)
            .returns(roomDataClassName.copy(nullable = true))
            .addCode(buildCodeBlock {
                add("""
                val selectedDirectory = DocumentFile.fromTreeUri(context, $selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  // selected directory does no exist
                  return null
                }
                val csvDocumentFiles = mutableListOf<DocumentFile>()
                csvFileNames.forEach {
                  val file = selectedDirectory.findFile(it)
                  if (file == null) {
                    // file was not found
                    return null
                  } else {
                    csvDocumentFiles.add(file)
                  }
                }
                
                """.trimIndent())
                entityDataList.forEachIndexed { i, entityData ->
                    val utilName = entityData.utilClassName.getDataName()
                    addStatement("")
                    addStatement("val %L = importCsvToRoomEntity(csvDocumentFiles[%L])", utilName, i)
                    addStatement("if (%L == null) return null // error importing data", utilName)
                }
                addStatement("")
                addStatement("return RoomData(")
                entityDataList.forEach { data ->
                    val utilName = data.utilClassName.getDataName()
                    val dataName = utilName.replace("RoomUtil", "")
                    addStatement(
                        format = "  %L = (%L·as·List<%L>).map·{·it.toOriginal()·},",
                        args = arrayOf(dataName, utilName, data.utilClassName.simpleName),
                    )
                }
                addStatement(")")
            })

        return funSpec
    }

    private fun buildImportCsvToRoomEntityFunction(): FunSpec.Builder {
        val csvFile = "csvFile"
        val csvReaderMemberName = MemberName("com.github.doyaaaaaken.kotlincsv.dsl", "csvReader")

        val funSpec = FunSpec.builder("importCsvToRoomEntity")
            .addParameter(csvFile, documentFileClassName)
            .returns(csvDataListClassName.copy(nullable = true))
            .addCode(buildCodeBlock {
                add("""
                val inputStream = context.contentResolver.openInputStream($csvFile.uri)
                  ?: return null // corrupt file
                try {
                  
                """.trimIndent())
                addStatement("val content = %M().readAll(inputStream)", csvReaderMemberName)
                addIndented {
                    add("""
                    if (content.size == 1) {
                      return emptyList()
                    }
                  
                    val header = content[0]
                    val rows = content.drop(1)
                    val entityData = mutableListOf<CsvData>()
                    when (header) {
                  
                    """.trimIndent())
                    addIndented {
                        entityDataList.forEach { entityData ->
                            add("""
                            %T.csvFieldToTypeMap.keys.toList() -> {
                              rows.forEach {
                                val entry = %T(
                            
                            """.trimIndent(), entityData.utilClassName, entityData.utilClassName)
                            addIndented {
                                addIndented {
                                    entityData.fieldInfoList.forEachIndexed { index, info ->
                                        val cast = getTypeCast(info.roomType)
                                        add("  %L = it[%L]%L,\n", info.fieldName, index, cast)
                                    }
                                }
                            }
                            add("""
                                )
                                entityData.add(entry)
                              }
                            }
                  
                            """.trimIndent())
                        }
                    }
                }
                add("""
                  }
                  return entityData
                } catch (e: Exception) {
                  return null // invalid data, wrong type data
                }
                """.trimIndent())
            })

        return funSpec
    }

    private fun getTypeCast(type: TypeName): String {
        val cast = when (type.removeKotlinPrefix()) {
            "Boolean" -> ".toBoolean()"
            "Boolean?" -> ".toBoolean()"
            "Short" -> ".toShort()"
            "Short?" -> ".toShortOrNull()"
            "Int" -> ".toInt()"
            "Int?" -> ".toIntOrNull()"
            "Long" -> ".toLong()"
            "Long?" -> ".toLongOrNull()"
            "Byte" -> ".toByte()"
            "Byte?" -> ".toByteOrNull()"
            "Char" -> ".single()"
            "Char?" -> ".singleOrNull()"
            "Double" -> ".toDouble()"
            "Double?" -> ".toDoubleOrNull()"
            "Float" -> ".toFloat()"
            "Float?" -> ".toFloatOrNull()"
            "ByteArray" -> ".toByteArray()"
            "ByteArray?" -> ".toByteArray()"
            else -> ""
        }
        return cast
    }

    private fun buildExportRoomToCsvFunction(): FunSpec.Builder {
        val saveDirectoryUri = "saveDirectoryUri"
        val roomData = "roomData"
        val funSpec = FunSpec.builder("exportRoomToCsv")
            .addParameter(saveDirectoryUri, uriClassName)
            .addParameter(roomData, roomDataClassName)
            .addCode(buildCodeBlock {
                addStatement(
                    "val saveDirectory = %T.fromTreeUri(%L, $saveDirectoryUri)!!",
                    documentFileClassName, CONTEXT_PROP,
                )
                add("""
                if (!saveDirectory.exists()) {
                  // given directory doesn't exist
                  return
                } else {
                  val newExportDirectory = createNewExportDirectory(saveDirectory)
                  if (newExportDirectory == null) {
                    // failed to create directory
                    return
                  } else {
                    val newCsvDocumentFiles = mutableListOf<DocumentFile>()
                    $roomData.csvDataMap.entries.forEach {
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

    private fun buildExportRoomEntityToCsv(): FunSpec.Builder {
        val newExportDirectory = "newExportDirectory"
        val csvWriterMemberName = MemberName("com.github.doyaaaaaken.kotlincsv.dsl", "csvWriter")
        val funSpec = FunSpec.builder("exportRoomEntityToCsv")
            .addModifiers(KModifier.PRIVATE)
            .returns(documentFileClassName)
            .addParameter(newExportDirectory, documentFileClassName)
            .addParameter("csvInfo", CsvInfo::class)
            .addParameter("csvDataList", csvDataListClassName)
            .addCode(buildCodeBlock {
                addStatement(
                    "val csvDocumentFile = %L.createFile(%S, csvInfo.csvFileName)!!",
                    newExportDirectory, "text/*",
                )
                addStatement(
                    "val outputStream = %L.contentResolver.openOutputStream(csvDocumentFile.uri)!!",
                    CONTEXT_PROP,
                )
                addStatement("%M().open(outputStream) {", csvWriterMemberName)
                add("""
                  writeRow(csvInfo.csvFieldToTypeMap.keys.toList())
                  csvDataList.forEach { writeRow(it.csvRow) }
                }
                return csvDocumentFile
                """.trimIndent())
            })

        return funSpec
    }

    private fun buildCreateNewExportDirectoryFunction(): FunSpec.Builder {
        val saveDirectory = "saveDirectory"
        val funSpec = FunSpec.builder("createNewExportDirectory")
            .addModifiers(KModifier.PRIVATE)
            .returns(documentFileClassName.copy(nullable = true))
            .addParameter(saveDirectory, documentFileClassName)
            .addCode(buildCodeBlock {
                addStatement(
                    "val sdf = %T(%S, %T.getDefault())",
                    SimpleDateFormat::class, "MMMM_dd_yyyy__hh_mm_aa", Locale::class,
                )
                addStatement("val formattedDate = sdf.format(%T())", Date::class)
                add("""
                val newExportDirectory = $saveDirectory.createDirectory(formattedDate)
                return newExportDirectory
                """.trimIndent())
            })

        return funSpec
    }

    private fun buildFindOrCreateSaveDirectoryFunction(): FunSpec.Builder {
        val saveDirectoryName = "saveDirectoryName"
        val selectedDirectoryUri = "selectedDirectoryUri"
        val funSpec = FunSpec.builder("findOrCreateSaveDirectory")
            .returns(uriClassName.copy(nullable = true))
            .addParameter(saveDirectoryName, String::class)
            .addParameter(selectedDirectoryUri, uriClassName)
            .addCode(buildCodeBlock {
                addStatement("try {")
                addIndented {
                    addStatement(
                        "val selectedDirectory = %T.fromTreeUri(%L, %L)!!",
                        documentFileClassName, CONTEXT_PROP, selectedDirectoryUri,
                    )
                    add("""
                    var saveDirectory = selectedDirectory.findFile($saveDirectoryName)
                    if (saveDirectory == null) {
                      saveDirectory = selectedDirectory.createDirectory($saveDirectoryName)!!
                    }
                    return saveDirectory.uri
                    
                    """.trimIndent())
                }
                addStatement("} catch (e: %T) {", Exception::class)
                addIndented { addStatement("return null") }
                addStatement("}")
            })

        return funSpec
    }

    init {
        createCsvConverterFile()
    }
}