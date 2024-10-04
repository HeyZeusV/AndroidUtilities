package com.heyzeusv.androidutilities.room.creators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.util.Constants.APP_DIRECTORY_NAME
import com.heyzeusv.androidutilities.room.util.Constants.CONTEXT
import com.heyzeusv.androidutilities.room.util.Constants.CSV_CONVERTER
import com.heyzeusv.androidutilities.room.util.Constants.EXTENSION_KT
import com.heyzeusv.androidutilities.room.util.Constants.ROOM_DATA
import com.heyzeusv.androidutilities.room.util.Constants.ROOM_UTIL_BASE
import com.heyzeusv.androidutilities.room.util.Constants.SELECTED_DIRECTORY_URI
import com.heyzeusv.androidutilities.room.util.Constants.TRUE
import com.heyzeusv.androidutilities.room.util.Constants.contextClassName
import com.heyzeusv.androidutilities.room.util.Constants.documentFileClassName
import com.heyzeusv.androidutilities.room.util.Constants.injectClassName
import com.heyzeusv.androidutilities.room.util.Constants.uriClassName
import com.heyzeusv.androidutilities.room.util.EntityInfo
import com.heyzeusv.androidutilities.room.util.CsvData
import com.heyzeusv.androidutilities.room.util.CsvInfo
import com.heyzeusv.androidutilities.room.util.addIndented
import com.heyzeusv.androidutilities.room.util.asListTypeName
import com.heyzeusv.androidutilities.room.util.getDataName
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
import com.squareup.kotlinpoet.buildCodeBlock

internal class CsvConverterCreator(
    private val codeGenerator: CodeGenerator,
    private val hiltOption: String?,
    private val dbClassDeclaration: KSClassDeclaration,
    private val entityInfoList: List<EntityInfo>,
    private val logger: KSPLogger,
) {
    private val packageName = dbClassDeclaration.getPackageName()

    private val roomDataClassName = ClassName(packageName, ROOM_DATA)
    private val csvDataListClassName = CsvData::class.asListTypeName()

    private fun createCsvConverterFile() {
        logger.info("Creating CsvConverter...")
        val fileBuilder = FileSpec.builder(packageName, CSV_CONVERTER)

        val classBuilder = TypeSpec.classBuilder(CSV_CONVERTER)
            .buildCsvConverter()

        fileBuilder.addType(classBuilder.build())

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dbClassDeclaration.containingFile!!),
            packageName = packageName,
            fileName = CSV_CONVERTER,
            extensionName = EXTENSION_KT,
        ).bufferedWriter().use { fileBuilder.build().writeTo(it) }
    }

    /**
     *  Builds CsvConverter TypeSpec by adding parameters/properties and functions.
     */
    private fun TypeSpec.Builder.buildCsvConverter(): TypeSpec.Builder {
        superclass(ClassName(packageName, ROOM_UTIL_BASE))
        addSuperclassConstructorParameter(CONTEXT)
        addSuperclassConstructorParameter(APP_DIRECTORY_NAME)

        // context parameter/property in order to read/write files
        val constructorBuilder = FunSpec.constructorBuilder()
            .addParameter(CONTEXT, contextClassName)
            .addParameter(APP_DIRECTORY_NAME, String::class)

        if (hiltOption?.lowercase() == TRUE) constructorBuilder.addAnnotation(injectClassName)

        primaryConstructor(constructorBuilder.build())
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
            PropertySpec.builder("csvFileNames", String::class.asListTypeName())
                .initializer(buildCodeBlock {
                    addStatement("listOf(")
                    addIndented {
                        val tableNames = entityInfoList.map { it.tableName }
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

        return this
    }

    private fun buildImportCsvToRoomFunction(): FunSpec.Builder {
        val funSpec = FunSpec.builder("importCsvToRoom")
            .addAnnotation(
                AnnotationSpec.builder(Suppress::class)
                    .addMember("%S", "UNCHECKED_CAST")
                    .build()
            )
            .addParameter(SELECTED_DIRECTORY_URI, uriClassName)
            .returns(roomDataClassName.copy(nullable = true))
            .addCode(buildCodeBlock {
                add("""
                    val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                    if (!selectedDirectory.exists()) {
                      // selected directory does not exist
                      return null
                    }
                    val csvDocumentFiles = mutableListOf<DocumentFile>()
                    csvFileNames.forEach {
                      val file = selectedDirectory.findFile(it) ?: return null // file was not found
                      csvDocumentFiles.add(file)
                    }
                    
                """.trimIndent())
                entityInfoList.forEachIndexed { i, entityInfo ->
                    val utilName = entityInfo.utilClassName.getDataName()
                    addStatement("")
                    addStatement("val %L = importCsvToRoomEntity(csvDocumentFiles[%L])", utilName, i)
                    addStatement("if (%L == null) return null // error importing data", utilName)
                }
                addStatement("")
                addStatement("return RoomData(")
                entityInfoList.forEach { data ->
                    val utilName = data.utilClassName.getDataName()
                    val dataName = utilName.replace("RoomUtil", "")
                    addStatement(
                        "  %L = (%L·as·List<%L>).map·{·it.toOriginal()·},",
                        dataName, utilName, data.utilClassName.simpleName,
                    )
                }
                addStatement(")")
            })

        return funSpec
    }

    private fun buildImportCsvToRoomEntityFunction(): FunSpec.Builder {
        val csvReaderMemberName = MemberName("com.github.doyaaaaaken.kotlincsv.dsl", "csvReader")

        val funSpec = FunSpec.builder("importCsvToRoomEntity")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("csvFile", documentFileClassName)
            .returns(csvDataListClassName.copy(nullable = true))
            .addCode(buildCodeBlock {
                add("""
                    val inputStream = context.contentResolver.openInputStream(csvFile.uri)
                      ?: return null // corrupt file
                    try {
                      val content = %M().readAll(inputStream)
                      if (content.size == 1) {
                        return emptyList()
                      }
                
                      val header = content[0]
                      val rows = content.drop(1)
                      val entityData = mutableListOf<CsvData>()
                      when (header) {
                        
                """.trimIndent(), csvReaderMemberName)
                addIndented {
                    addIndented {
                        entityInfoList.forEach { entityInfo ->
                            add("""
                                %T.csvFieldToTypeMap.keys.toList() -> {
                                  rows.forEach {
                                    val entry = %T(
                            
                            """.trimIndent(), entityInfo.utilClassName, entityInfo.utilClassName)
                            addIndented {
                                addIndented {
                                    entityInfo.fieldInfoList.forEachIndexed { index, info ->
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
        val funSpec = FunSpec.builder("exportRoomToCsv")
            .addParameter("appExportDirectoryUri", uriClassName)
            .addParameter("roomData", roomDataClassName)
            .addCode(buildCodeBlock {
                add("""
                    val appExportDirectory = %T.fromTreeUri(%L, appExportDirectoryUri)!!
                    if (!appExportDirectory.exists()) {
                      // given directory doesn't exist
                      return
                    } else {
                      // returns if fails to create directory
                      val newExportDirectory = createNewDirectory(appExportDirectory) ?: return
                      val newCsvFiles = mutableListOf<DocumentFile>()
                      roomData.csvDataMap.entries.forEach {
                        val csvFile = exportRoomEntityToCsv(
                          newExportDirectory = newExportDirectory,
                          csvInfo = it.key,
                          csvDataList = it.value,
                        )
                        if (csvFile == null) {
                          // delete previously created csv files
                          newCsvFiles.forEach { file -> file.delete() }
                          // delete directory created for this export
                          newExportDirectory.delete()
                          return // failed to create csv file
                        }
                        newCsvFiles.add(csvFile)
                      }
                    }
                """.trimIndent(), documentFileClassName, CONTEXT)
            })

        return funSpec
    }

    private fun buildExportRoomEntityToCsv(): FunSpec.Builder {
        val csvWriterMemberName = MemberName("com.github.doyaaaaaken.kotlincsv.dsl", "csvWriter")

        val funSpec = FunSpec.builder("exportRoomEntityToCsv")
            .addModifiers(KModifier.PRIVATE)
            .returns(documentFileClassName.copy(nullable = true))
            .addParameter("newExportDirectory", documentFileClassName)
            .addParameter("csvInfo", CsvInfo::class)
            .addParameter("csvDataList", csvDataListClassName)
            .addCode(buildCodeBlock {
                add("""
                    val csvFile = newExportDirectory.createFile("text/*", csvInfo.csvFileName) ?:
                      return null // failed to create file
                    val outputStream = context.contentResolver.openOutputStream(csvFile.uri) ?:
                      return null // failed to open output stream
                    
                    %M().open(outputStream) {
                      writeRow(csvInfo.csvFieldToTypeMap.keys.toList())
                      csvDataList.forEach { writeRow(it.csvRow) }
                    }
                    return csvFile
                """.trimIndent(), csvWriterMemberName)
            })

        return funSpec
    }

    init {
        createCsvConverterFile()
    }
}