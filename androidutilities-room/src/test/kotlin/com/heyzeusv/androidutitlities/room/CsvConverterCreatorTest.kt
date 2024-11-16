package com.heyzeusv.androidutitlities.room

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import kotlin.test.assertEquals

/**
 *  Used [this article](https://dev.to/chigichan24/why-dont-you-write-unit-tests-and-integration-tests-to-ksp-project-2oio)
 *  as a guide to write these tests.
 */
@OptIn(ExperimentalCompilerApi::class)
class CsvConverterCreatorTest : CreatorTestBase() {

    @Test
    fun `Generate CsvConverter for single entity database`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "BasicTwoField.kt",
                contents = """
                    package test.entity
                    
                    import androidx.room.Entity
                    
                    @Entity
                    class BasicTwoField(
                        val intField: Int = 0,
                        val stringField: String = "",
                    )
                """
            ),
            dummyDb,
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(8, kspCompileResult.generatedFiles.size)
        kspCompileResult.assertFileEquals(expectedSingleEntityCsvConverter, "CsvConverter.kt")
    }

    @Test
    fun `Generate CsvConverter for multiple entity database`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "Entities.kt",
                contents = """
                    package test.entity
                    
                    import androidx.room.Entity
                    
                    @Entity
                    class EntityOne(
                        val intField: Int = 0,
                        val stringField: String = "",
                    )

                    @Entity
                    class EntityTwo(
                        val longField: Long = 0L,
                        val charField: Char = '',
                    )

                    @Entity
                    class EntityThree(
                        val floatField: Float = 0f,
                        val doubleField: Double = 0.0,
                    )

                    @Entity
                    class EntityFour(
                        val booleanField: Boolean = true,
                        val byteField: Byte = 0x00,
                    )
                """
            ),
            dummyDb,
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(11, kspCompileResult.generatedFiles.size)
        kspCompileResult.assertFileEquals(expectedMultiEntityCsvConverter, "CsvConverter.kt")
    }

    @Test
    fun `Generate CsvConverter with Hilt inject when roomUtilHilt option is true`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "BasicTwoField.kt",
                contents = """
                    package test.entity
                    
                    import androidx.room.Entity
                    
                    @Entity
                    class BasicTwoField(
                        val intField: Int = 0,
                        val stringField: String = "",
                    )
                """
            ),
            dummyDb,
            kspArguments = mutableMapOf("roomUtilHilt" to "TrUe"),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(8, kspCompileResult.generatedFiles.size)
        kspCompileResult.assertFileEquals(expectedCsvConverterWithHiltOptionValue, "CsvConverter.kt")
    }

    @Test
    fun `Do not generate CsvConverter when roomUtilCsv option is false`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "BasicTwoField.kt",
                contents = """
                    package test.entity
                    
                    import androidx.room.Entity
                    
                    @Entity
                    class BasicTwoField(
                        val intField: Int = 0,
                        val stringField: String = "",
                    )
                """
            ),
            dummyDb,
            kspArguments = mutableMapOf("roomUtilCsv" to "FaLsE"),
            )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(3, kspCompileResult.generatedFiles.size)
    }

    companion object {
        val expectedSingleEntityCsvConverter = """
            package test

            import android.content.Context
            import android.net.Uri
            import androidx.documentfile.provider.DocumentFile
            import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
            import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
            import kotlin.String
            import kotlin.Suppress
            import kotlin.collections.List
            import test.RoomUtilStatus.Error
            import test.RoomUtilStatus.Progress
            import test.RoomUtilStatus.Success
            import test.entity.BasicTwoFieldRoomUtil
            
            public class CsvConverter(
              private val context: Context,
              private val appDirectoryName: String,
            ) : RoomUtilBase(context, appDirectoryName) {
              private val csvFileNames: List<String> = listOf(
                "BasicTwoField.csv", 
              )
            
              @Suppress("UNCHECKED_CAST")
              public fun importCsvToRoom(selectedDirectoryUri: Uri): RoomData? {
                _status.value = Progress(R.string.import_progress_started)
                val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  _status.value = Error(R.string.import_error_missing_directory)
                  return null
                }
                val csvDocumentFiles = mutableListOf<DocumentFile>()
                csvFileNames.forEach {
                  val file = selectedDirectory.findFile(it)
                  if (file == null) {
                    _status.value = Error(R.string.import_error_missing_file, it)
                    return null
                  } else {
                    csvDocumentFiles.add(file)
                  }
                }
            
                val basicTwoFieldRoomUtilStatus = importCsvToRoomEntity(csvDocumentFiles[0])
                if (basicTwoFieldRoomUtilStatus == null) return null
            
                return RoomData(
                  basicTwoFieldData =
                    (basicTwoFieldRoomUtilStatus as List<BasicTwoFieldRoomUtil>).map { it.toOriginal() },
                )
              }
            
              private fun importCsvToRoomEntity(csvFile: DocumentFile): List<CsvData>? {
                val inputStream = context.contentResolver.openInputStream(csvFile.uri)
                if (inputStream == null) {
                  _status.value = Error(R.string.import_error_corrupt_file, csvFile.name!!)
                  return null
                }
                try {
                  val content = csvReader().readAll(inputStream)
                  if (content.size == 1) {
                    _status.value = Progress(R.string.import_progress_entity_success, csvFile.name!!)
                    return mutableListOf()
                  }
            
                  val header = content[0]
                  val rows = content.drop(1)
                  val entityData = mutableListOf<CsvData>()
                  when (header) {
                    BasicTwoFieldRoomUtil.csvFieldToTypeMap.keys.toList() -> {
                      rows.forEach {
                        val entry = BasicTwoFieldRoomUtil(
                          intField = it[0].toInt(),
                          stringField = it[1],
                        )
                        entityData.add(entry)
                      }
                    }
                  }
                  _status.value = Progress(R.string.import_progress_entity_success, csvFile.name!!)
                  return entityData
                } catch (e: Exception) {
                  _status.value = Error(R.string.import_error_invalid_data, csvFile.name!!)
                  return null
                }
              }
            
              public fun exportRoomToCsv(appExportDirectoryUri: Uri, roomData: RoomData) {
                _status.value = Progress(R.string.export_progress_started)
                val appExportDirectory = DocumentFile.fromTreeUri(context, appExportDirectoryUri)!!
                if (!appExportDirectory.exists()) {
                  _status.value = Error(R.string.export_error_missing_directory)
                  return
                } else {
                  // returns if fails to create directory
                  val newExportDirectory = createNewDirectory(appExportDirectory)
                  if (newExportDirectory == null) {
                    _status.value = Error(R.string.export_error_create_directory_failed)
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
                  _status.value = Success(R.string.export_success)
                }
              }
            
              private fun exportRoomEntityToCsv(
                newExportDirectory: DocumentFile,
                csvInfo: CsvInfo,
                csvDataList: List<CsvData>,
              ): DocumentFile? {
                val csvFile = newExportDirectory.createFile("text/*", csvInfo.csvFileName)
                if (csvFile == null) {
                  _status.value = Error(
                    messageId = R.string.export_error_create_file_failed,
                    name = csvInfo.csvFileName,
                  )
                  return null
                }
                val outputStream = context.contentResolver.openOutputStream(csvFile.uri)
                if (outputStream == null) {
                  _status.value = Error(
                    messageId = R.string.export_error_failed,
                    name = csvInfo.csvFileName,
                  )
                  return null
                }
                csvWriter().open(outputStream) {
                  writeRow(csvInfo.csvFieldToTypeMap.keys.toList())
                  csvDataList.forEach { writeRow(it.csvRow) }
                }
                _status.value = Progress(
                  messageId = R.string.export_progress_entity_success,
                  name = csvInfo.csvFileName,
                )
                return csvFile
              }
            }
        """.trimIndent()

        val expectedMultiEntityCsvConverter = """
            package test

            import android.content.Context
            import android.net.Uri
            import androidx.documentfile.provider.DocumentFile
            import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
            import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
            import kotlin.String
            import kotlin.Suppress
            import kotlin.collections.List
            import test.RoomUtilStatus.Error
            import test.RoomUtilStatus.Progress
            import test.RoomUtilStatus.Success
            import test.entity.EntityFourRoomUtil
            import test.entity.EntityOneRoomUtil
            import test.entity.EntityThreeRoomUtil
            import test.entity.EntityTwoRoomUtil
            
            public class CsvConverter(
              private val context: Context,
              private val appDirectoryName: String,
            ) : RoomUtilBase(context, appDirectoryName) {
              private val csvFileNames: List<String> = listOf(
                "EntityOne.csv", "EntityTwo.csv", "EntityThree.csv", "EntityFour.csv", 
              )
            
              @Suppress("UNCHECKED_CAST")
              public fun importCsvToRoom(selectedDirectoryUri: Uri): RoomData? {
                _status.value = Progress(R.string.import_progress_started)
                val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  _status.value = Error(R.string.import_error_missing_directory)
                  return null
                }
                val csvDocumentFiles = mutableListOf<DocumentFile>()
                csvFileNames.forEach {
                  val file = selectedDirectory.findFile(it)
                  if (file == null) {
                    _status.value = Error(R.string.import_error_missing_file, it)
                    return null
                  } else {
                    csvDocumentFiles.add(file)
                  }
                }
            
                val entityOneRoomUtilStatus = importCsvToRoomEntity(csvDocumentFiles[0])
                if (entityOneRoomUtilStatus == null) return null
            
                val entityTwoRoomUtilStatus = importCsvToRoomEntity(csvDocumentFiles[1])
                if (entityTwoRoomUtilStatus == null) return null
            
                val entityThreeRoomUtilStatus = importCsvToRoomEntity(csvDocumentFiles[2])
                if (entityThreeRoomUtilStatus == null) return null
            
                val entityFourRoomUtilStatus = importCsvToRoomEntity(csvDocumentFiles[3])
                if (entityFourRoomUtilStatus == null) return null
            
                return RoomData(
                  entityOneData = (entityOneRoomUtilStatus as List<EntityOneRoomUtil>).map { it.toOriginal() },
                  entityTwoData = (entityTwoRoomUtilStatus as List<EntityTwoRoomUtil>).map { it.toOriginal() },
                  entityThreeData =
                    (entityThreeRoomUtilStatus as List<EntityThreeRoomUtil>).map { it.toOriginal() },
                  entityFourData =
                    (entityFourRoomUtilStatus as List<EntityFourRoomUtil>).map { it.toOriginal() },
                )
              }
            
              private fun importCsvToRoomEntity(csvFile: DocumentFile): List<CsvData>? {
                val inputStream = context.contentResolver.openInputStream(csvFile.uri)
                if (inputStream == null) {
                  _status.value = Error(R.string.import_error_corrupt_file, csvFile.name!!)
                  return null
                }
                try {
                  val content = csvReader().readAll(inputStream)
                  if (content.size == 1) {
                    _status.value = Progress(R.string.import_progress_entity_success, csvFile.name!!)
                    return mutableListOf()
                  }
            
                  val header = content[0]
                  val rows = content.drop(1)
                  val entityData = mutableListOf<CsvData>()
                  when (header) {
                    EntityOneRoomUtil.csvFieldToTypeMap.keys.toList() -> {
                      rows.forEach {
                        val entry = EntityOneRoomUtil(
                          intField = it[0].toInt(),
                          stringField = it[1],
                        )
                        entityData.add(entry)
                      }
                    }
                    EntityTwoRoomUtil.csvFieldToTypeMap.keys.toList() -> {
                      rows.forEach {
                        val entry = EntityTwoRoomUtil(
                          longField = it[0].toLong(),
                          charField = it[1].single(),
                        )
                        entityData.add(entry)
                      }
                    }
                    EntityThreeRoomUtil.csvFieldToTypeMap.keys.toList() -> {
                      rows.forEach {
                        val entry = EntityThreeRoomUtil(
                          floatField = it[0].toFloat(),
                          doubleField = it[1].toDouble(),
                        )
                        entityData.add(entry)
                      }
                    }
                    EntityFourRoomUtil.csvFieldToTypeMap.keys.toList() -> {
                      rows.forEach {
                        val entry = EntityFourRoomUtil(
                          booleanField = it[0].toBoolean(),
                          byteField = it[1].toByte(),
                        )
                        entityData.add(entry)
                      }
                    }
                  }
                  _status.value = Progress(R.string.import_progress_entity_success, csvFile.name!!)
                  return entityData
                } catch (e: Exception) {
                  _status.value = Error(R.string.import_error_invalid_data, csvFile.name!!)
                  return null
                }
              }
            
              public fun exportRoomToCsv(appExportDirectoryUri: Uri, roomData: RoomData) {
                _status.value = Progress(R.string.export_progress_started)
                val appExportDirectory = DocumentFile.fromTreeUri(context, appExportDirectoryUri)!!
                if (!appExportDirectory.exists()) {
                  _status.value = Error(R.string.export_error_missing_directory)
                  return
                } else {
                  // returns if fails to create directory
                  val newExportDirectory = createNewDirectory(appExportDirectory)
                  if (newExportDirectory == null) {
                    _status.value = Error(R.string.export_error_create_directory_failed)
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
                  _status.value = Success(R.string.export_success)
                }
              }
            
              private fun exportRoomEntityToCsv(
                newExportDirectory: DocumentFile,
                csvInfo: CsvInfo,
                csvDataList: List<CsvData>,
              ): DocumentFile? {
                val csvFile = newExportDirectory.createFile("text/*", csvInfo.csvFileName)
                if (csvFile == null) {
                  _status.value = Error(
                    messageId = R.string.export_error_create_file_failed,
                    name = csvInfo.csvFileName,
                  )
                  return null
                }
                val outputStream = context.contentResolver.openOutputStream(csvFile.uri)
                if (outputStream == null) {
                  _status.value = Error(
                    messageId = R.string.export_error_failed,
                    name = csvInfo.csvFileName,
                  )
                  return null
                }
                csvWriter().open(outputStream) {
                  writeRow(csvInfo.csvFieldToTypeMap.keys.toList())
                  csvDataList.forEach { writeRow(it.csvRow) }
                }
                _status.value = Progress(
                  messageId = R.string.export_progress_entity_success,
                  name = csvInfo.csvFileName,
                )
                return csvFile
              }
            }
        """.trimIndent()

        val expectedCsvConverterWithHiltOptionValue = """
            package test

            import android.content.Context
            import android.net.Uri
            import androidx.documentfile.provider.DocumentFile
            import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
            import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
            import javax.inject.Inject
            import kotlin.String
            import kotlin.Suppress
            import kotlin.collections.List
            import test.RoomUtilStatus.Error
            import test.RoomUtilStatus.Progress
            import test.RoomUtilStatus.Success
            import test.entity.BasicTwoFieldRoomUtil
            
            public class CsvConverter @Inject constructor(
              private val context: Context,
              private val appDirectoryName: String,
            ) : RoomUtilBase(context, appDirectoryName) {
              private val csvFileNames: List<String> = listOf(
                "BasicTwoField.csv", 
              )
            
              @Suppress("UNCHECKED_CAST")
              public fun importCsvToRoom(selectedDirectoryUri: Uri): RoomData? {
                _status.value = Progress(R.string.import_progress_started)
                val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  _status.value = Error(R.string.import_error_missing_directory)
                  return null
                }
                val csvDocumentFiles = mutableListOf<DocumentFile>()
                csvFileNames.forEach {
                  val file = selectedDirectory.findFile(it)
                  if (file == null) {
                    _status.value = Error(R.string.import_error_missing_file, it)
                    return null
                  } else {
                    csvDocumentFiles.add(file)
                  }
                }
            
                val basicTwoFieldRoomUtilStatus = importCsvToRoomEntity(csvDocumentFiles[0])
                if (basicTwoFieldRoomUtilStatus == null) return null
            
                return RoomData(
                  basicTwoFieldData =
                    (basicTwoFieldRoomUtilStatus as List<BasicTwoFieldRoomUtil>).map { it.toOriginal() },
                )
              }
            
              private fun importCsvToRoomEntity(csvFile: DocumentFile): List<CsvData>? {
                val inputStream = context.contentResolver.openInputStream(csvFile.uri)
                if (inputStream == null) {
                  _status.value = Error(R.string.import_error_corrupt_file, csvFile.name!!)
                  return null
                }
                try {
                  val content = csvReader().readAll(inputStream)
                  if (content.size == 1) {
                    _status.value = Progress(R.string.import_progress_entity_success, csvFile.name!!)
                    return mutableListOf()
                  }
            
                  val header = content[0]
                  val rows = content.drop(1)
                  val entityData = mutableListOf<CsvData>()
                  when (header) {
                    BasicTwoFieldRoomUtil.csvFieldToTypeMap.keys.toList() -> {
                      rows.forEach {
                        val entry = BasicTwoFieldRoomUtil(
                          intField = it[0].toInt(),
                          stringField = it[1],
                        )
                        entityData.add(entry)
                      }
                    }
                  }
                  _status.value = Progress(R.string.import_progress_entity_success, csvFile.name!!)
                  return entityData
                } catch (e: Exception) {
                  _status.value = Error(R.string.import_error_invalid_data, csvFile.name!!)
                  return null
                }
              }
            
              public fun exportRoomToCsv(appExportDirectoryUri: Uri, roomData: RoomData) {
                _status.value = Progress(R.string.export_progress_started)
                val appExportDirectory = DocumentFile.fromTreeUri(context, appExportDirectoryUri)!!
                if (!appExportDirectory.exists()) {
                  _status.value = Error(R.string.export_error_missing_directory)
                  return
                } else {
                  // returns if fails to create directory
                  val newExportDirectory = createNewDirectory(appExportDirectory)
                  if (newExportDirectory == null) {
                    _status.value = Error(R.string.export_error_create_directory_failed)
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
                  _status.value = Success(R.string.export_success)
                }
              }
            
              private fun exportRoomEntityToCsv(
                newExportDirectory: DocumentFile,
                csvInfo: CsvInfo,
                csvDataList: List<CsvData>,
              ): DocumentFile? {
                val csvFile = newExportDirectory.createFile("text/*", csvInfo.csvFileName)
                if (csvFile == null) {
                  _status.value = Error(
                    messageId = R.string.export_error_create_file_failed,
                    name = csvInfo.csvFileName,
                  )
                  return null
                }
                val outputStream = context.contentResolver.openOutputStream(csvFile.uri)
                if (outputStream == null) {
                  _status.value = Error(
                    messageId = R.string.export_error_failed,
                    name = csvInfo.csvFileName,
                  )
                  return null
                }
                csvWriter().open(outputStream) {
                  writeRow(csvInfo.csvFieldToTypeMap.keys.toList())
                  csvDataList.forEach { writeRow(it.csvRow) }
                }
                _status.value = Progress(
                  messageId = R.string.export_progress_entity_success,
                  name = csvInfo.csvFileName,
                )
                return csvFile
              }
            }
        """.trimIndent()
    }
}