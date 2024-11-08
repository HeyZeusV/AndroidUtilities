package com.heyzeusv.androidutilities.room.creators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.util.Constants.APP_DIRECTORY_NAME
import com.heyzeusv.androidutilities.room.util.Constants.CONTEXT
import com.heyzeusv.androidutilities.room.util.Constants.CSV_CONVERTER
import com.heyzeusv.androidutilities.room.util.Constants.CSV_DATA
import com.heyzeusv.androidutilities.room.util.Constants.CSV_INFO
import com.heyzeusv.androidutilities.room.util.Constants.EXTENSION_KT
import com.heyzeusv.androidutilities.room.util.Constants.ROOM_DATA
import com.heyzeusv.androidutilities.room.util.Constants.ROOM_UTIL_BASE
import com.heyzeusv.androidutilities.room.util.Constants.ROOM_UTIL_STATUS
import com.heyzeusv.androidutilities.room.util.Constants.SELECTED_DIRECTORY_URI
import com.heyzeusv.androidutilities.room.util.Constants.STATUS_ERROR
import com.heyzeusv.androidutilities.room.util.Constants.STATUS_PROGRESS
import com.heyzeusv.androidutilities.room.util.Constants.STATUS_SUCCESS
import com.heyzeusv.androidutilities.room.util.Constants.TRUE
import com.heyzeusv.androidutilities.room.util.Constants.contextClassName
import com.heyzeusv.androidutilities.room.util.Constants.documentFileClassName
import com.heyzeusv.androidutilities.room.util.Constants.injectClassName
import com.heyzeusv.androidutilities.room.util.Constants.uriClassName
import com.heyzeusv.androidutilities.room.util.EntityInfo
import com.heyzeusv.androidutilities.room.util.addIndented
import com.heyzeusv.androidutilities.room.util.asListTypeName
import com.heyzeusv.androidutilities.room.util.getDataName
import com.heyzeusv.androidutilities.room.util.getPackageName
import com.heyzeusv.androidutilities.room.util.getStatusName
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
    private val resourceClassName: ClassName,
    private val logger: KSPLogger,
) {
    private val packageName = dbClassDeclaration.getPackageName()

    private val roomDataClassName = ClassName(packageName, ROOM_DATA)
    private val csvDataClassName = ClassName(packageName, CSV_DATA)
    private val csvInfoClassName = ClassName(packageName, CSV_INFO)
    private val statusProgressClassName = ClassName("$packageName.$ROOM_UTIL_STATUS", STATUS_PROGRESS)
    private val statusErrorClassName = ClassName("$packageName.$ROOM_UTIL_STATUS", STATUS_ERROR)
    private val statusSuccessClassName = ClassName("$packageName.$ROOM_UTIL_STATUS", STATUS_SUCCESS)

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
                    _status.value = %T(R.string.status_progress_import_started)
                    val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                    if (!selectedDirectory.exists()) {
                      _status.value = %T(%T.string.status_error_import_missing_directory)
                      return null
                    }
                    val csvDocumentFiles = mutableListOf<DocumentFile>()
                    csvFileNames.forEach {
                      val file = selectedDirectory.findFile(it)
                      if (file == null) {
                        _status.value = Error(R.string.status_error_import_missing_file)
                        return null
                      } else {
                        csvDocumentFiles.add(file)
                      }
                    }
                    
                """.trimIndent(), statusProgressClassName, statusErrorClassName, resourceClassName)
                entityInfoList.forEachIndexed { i, entityInfo ->
                    val utilName = entityInfo.utilClassName.getStatusName()
                    add("""
                        
                        val %L = importCsvToRoomEntity(csvDocumentFiles[%L])
                        if (%L == null) return null
                        
                    """.trimIndent(), utilName, i, utilName)
                }
                addStatement("")
                addStatement("return RoomData(")
                entityInfoList.forEach { data ->
                    val utilName = data.utilClassName.getStatusName()
                    val dataName = data.utilClassName.getDataName().replace("RoomUtil", "")
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
            .returns(csvDataClassName.asListTypeName().copy(nullable = true))
            .addCode(buildCodeBlock {
                add("""
                    val inputStream = context.contentResolver.openInputStream(csvFile.uri)
                    if (inputStream == null) {
                      _status.value = Error(R.string.status_error_import_corrupt_file, csvFile.name!!)
                      return null
                    }
                    try {
                      val content = %M().readAll(inputStream)
                      if (content.size == 1) {
                        _status.value = Progress(R.string.status_progress_import_entity_success,csvFile.name!!)
                        return mutableListOf()
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
                      _status.value = Progress(R.string.status_progress_import_entity_success, csvFile.name!!)
                      return entityData
                    } catch (e: Exception) {
                      _status.value = Error(R.string.status_error_import_invalid_data, csvFile.name!!)
                      return null
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
                    _status.value = Progress(R.string.status_progress_export_started)
                    val appExportDirectory = %T.fromTreeUri(%L, appExportDirectoryUri)!!
                    if (!appExportDirectory.exists()) {
                      _status.value = Error(R.string.status_error_export_missing_directory)
                      return
                    } else {
                      // returns if fails to create directory
                      val newExportDirectory = createNewDirectory(appExportDirectory)
                      if (newExportDirectory == null) {
                        _status.value = Error(R.string.status_error_export_create_directory_failed)
                        return
                      }
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
                      _status.value = %T(R.string.status_success_export)
                    }
                """.trimIndent(), documentFileClassName, CONTEXT, statusSuccessClassName)
            })

        return funSpec
    }

    private fun buildExportRoomEntityToCsv(): FunSpec.Builder {
        val csvWriterMemberName = MemberName("com.github.doyaaaaaken.kotlincsv.dsl", "csvWriter")

        val funSpec = FunSpec.builder("exportRoomEntityToCsv")
            .addModifiers(KModifier.PRIVATE)
            .returns(documentFileClassName.copy(nullable = true))
            .addParameter("newExportDirectory", documentFileClassName)
            .addParameter("csvInfo", csvInfoClassName)
            .addParameter("csvDataList", csvDataClassName.asListTypeName())
            .addCode(buildCodeBlock {
                add("""
                    val csvFile = newExportDirectory.createFile("text/*", csvInfo.csvFileName)
                    if (csvFile == null) {
                      _status.value = Error(
                        messageId = R.string.status_error_export_create_file_failed,
                        name = csvInfo.csvFileName,
                      )
                      return null
                    }
                    val outputStream = context.contentResolver.openOutputStream(csvFile.uri)
                    if (outputStream == null) {
                      _status.value = Error(
                        messageId = R.string.status_error_export_failed,
                        name = csvInfo.csvFileName,
                      )
                      return null
                    }
                    %M().open(outputStream) {
                      writeRow(csvInfo.csvFieldToTypeMap.keys.toList())
                      csvDataList.forEach { writeRow(it.csvRow) }
                    }
                    _status.value = Progress(
                      messageId = R.string.status_progress_export_entity_success,
                      name = csvInfo.csvFileName,
                    )
                    return csvFile
                """.trimIndent(), csvWriterMemberName)
            })

        return funSpec
    }

    init {
        createCsvConverterFile()
    }
}