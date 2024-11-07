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
        assertEquals(2, kspCompileResult.generatedFiles.size)
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
              public fun importCsvToRoom(selectedDirectoryUri: Uri): RoomUtilStatus {
                val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  return Error(R.string.status_error_import_missing_directory)
                }
                val csvDocumentFiles = mutableListOf<DocumentFile>()
                csvFileNames.forEach {
                  val file = selectedDirectory.findFile(it) ?:
                    return Error(R.string.status_error_import_missing_file)
                  csvDocumentFiles.add(file)
                }

                val basicTwoFieldRoomUtilStatus = importCsvToRoomEntity(csvDocumentFiles[0])
                if (basicTwoFieldRoomUtilStatus is Error) return basicTwoFieldRoomUtilStatus

                return Success(
                  messageId = R.string.status_success_import,
                  dbData = RoomData(
                    basicTwoFieldData =
                    ((basicTwoFieldRoomUtilStatus as Progress).dbData as List<BasicTwoFieldRoomUtil>).map { it.toOriginal() },
                  )
                )
              }

              private fun importCsvToRoomEntity(csvFile: DocumentFile): RoomUtilStatus {
                val inputStream = context.contentResolver.openInputStream(csvFile.uri)
                  ?: return Error(R.string.status_error_import_corrupt_file, csvFile.name!!)
                try {
                  val content = csvReader().readAll(inputStream)
                  if (content.size == 1) {
                    return Progress(
                      messageId = R.string.status_progress_import_entity_success,
                      name = csvFile.name!!,
                      dbData = emptyList<CsvInfo>(),
                    )
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
                  return Progress(
                    messageId = R.string.status_progress_import_entity_success,
                    name = csvFile.name!!,
                    dbData = emptyList<CsvInfo>(),
                  )
                } catch (e: Exception) {
                  return Error(R.string.status_error_import_invalid_data, csvFile.name!!)
                }
              }

              public fun exportRoomToCsv(appExportDirectoryUri: Uri, roomData: RoomData) {
                val appExportDirectory = DocumentFile.fromTreeUri(context, appExportDirectoryUri)!!
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
              }

              private fun exportRoomEntityToCsv(
                newExportDirectory: DocumentFile,
                csvInfo: CsvInfo,
                csvDataList: List<CsvData>,
              ): DocumentFile? {
                val csvFile = newExportDirectory.createFile("text/*", csvInfo.csvFileName) ?:
                  return null // failed to create file
                val outputStream = context.contentResolver.openOutputStream(csvFile.uri) ?:
                  return null // failed to open output stream

                csvWriter().open(outputStream) {
                  writeRow(csvInfo.csvFieldToTypeMap.keys.toList())
                  csvDataList.forEach { writeRow(it.csvRow) }
                }
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
              public fun importCsvToRoom(selectedDirectoryUri: Uri): RoomUtilStatus {
                val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  return Error(R.string.status_error_import_missing_directory)
                }
                val csvDocumentFiles = mutableListOf<DocumentFile>()
                csvFileNames.forEach {
                  val file = selectedDirectory.findFile(it) ?:
                    return Error(R.string.status_error_import_missing_file)
                  csvDocumentFiles.add(file)
                }

                val entityOneRoomUtilStatus = importCsvToRoomEntity(csvDocumentFiles[0])
                if (entityOneRoomUtilStatus is Error) return entityOneRoomUtilStatus
            
                val entityTwoRoomUtilStatus = importCsvToRoomEntity(csvDocumentFiles[1])
                if (entityTwoRoomUtilStatus is Error) return entityTwoRoomUtilStatus
            
                val entityThreeRoomUtilStatus = importCsvToRoomEntity(csvDocumentFiles[2])
                if (entityThreeRoomUtilStatus is Error) return entityThreeRoomUtilStatus
            
                val entityFourRoomUtilStatus = importCsvToRoomEntity(csvDocumentFiles[3])
                if (entityFourRoomUtilStatus is Error) return entityFourRoomUtilStatus

                return Success(
                  messageId = R.string.status_success_import,
                  dbData = RoomData(
                    entityOneData =
                    ((entityOneRoomUtilStatus as Progress).dbData as List<EntityOneRoomUtil>).map { it.toOriginal() },
                    entityTwoData =
                    ((entityTwoRoomUtilStatus as Progress).dbData as List<EntityTwoRoomUtil>).map { it.toOriginal() },
                    entityThreeData =
                    ((entityThreeRoomUtilStatus as Progress).dbData as List<EntityThreeRoomUtil>).map { it.toOriginal() },
                    entityFourData =
                    ((entityFourRoomUtilStatus as Progress).dbData as List<EntityFourRoomUtil>).map { it.toOriginal() },
                  )
                )
              }

              private fun importCsvToRoomEntity(csvFile: DocumentFile): RoomUtilStatus {
                val inputStream = context.contentResolver.openInputStream(csvFile.uri)
                  ?: return Error(R.string.status_error_import_corrupt_file, csvFile.name!!)
                try {
                  val content = csvReader().readAll(inputStream)
                  if (content.size == 1) {
                    return Progress(
                      messageId = R.string.status_progress_import_entity_success,
                      name = csvFile.name!!,
                      dbData = emptyList<CsvInfo>(),
                    )
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
                  return Progress(
                    messageId = R.string.status_progress_import_entity_success,
                    name = csvFile.name!!,
                    dbData = emptyList<CsvInfo>(),
                  )
                } catch (e: Exception) {
                  return Error(R.string.status_error_import_invalid_data, csvFile.name!!)
                }
              }

              public fun exportRoomToCsv(appExportDirectoryUri: Uri, roomData: RoomData) {
                val appExportDirectory = DocumentFile.fromTreeUri(context, appExportDirectoryUri)!!
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
              }

              private fun exportRoomEntityToCsv(
                newExportDirectory: DocumentFile,
                csvInfo: CsvInfo,
                csvDataList: List<CsvData>,
              ): DocumentFile? {
                val csvFile = newExportDirectory.createFile("text/*", csvInfo.csvFileName) ?:
                  return null // failed to create file
                val outputStream = context.contentResolver.openOutputStream(csvFile.uri) ?:
                  return null // failed to open output stream

                csvWriter().open(outputStream) {
                  writeRow(csvInfo.csvFieldToTypeMap.keys.toList())
                  csvDataList.forEach { writeRow(it.csvRow) }
                }
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
              public fun importCsvToRoom(selectedDirectoryUri: Uri): RoomUtilStatus {
                val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  return Error(R.string.status_error_import_missing_directory)
                }
                val csvDocumentFiles = mutableListOf<DocumentFile>()
                csvFileNames.forEach {
                  val file = selectedDirectory.findFile(it) ?:
                    return Error(R.string.status_error_import_missing_file)
                  csvDocumentFiles.add(file)
                }

                val basicTwoFieldRoomUtilStatus = importCsvToRoomEntity(csvDocumentFiles[0])
                if (basicTwoFieldRoomUtilStatus is Error) return basicTwoFieldRoomUtilStatus

                return Success(
                  messageId = R.string.status_success_import,
                  dbData = RoomData(
                    basicTwoFieldData =
                    ((basicTwoFieldRoomUtilStatus as Progress).dbData as List<BasicTwoFieldRoomUtil>).map { it.toOriginal() },
                  )
                )
              }

              private fun importCsvToRoomEntity(csvFile: DocumentFile): RoomUtilStatus {
                val inputStream = context.contentResolver.openInputStream(csvFile.uri)
                  ?: return Error(R.string.status_error_import_corrupt_file, csvFile.name!!)
                try {
                  val content = csvReader().readAll(inputStream)
                  if (content.size == 1) {
                    return Progress(
                      messageId = R.string.status_progress_import_entity_success,
                      name = csvFile.name!!,
                      dbData = emptyList<CsvInfo>(),
                    )
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
                  return Progress(
                    messageId = R.string.status_progress_import_entity_success,
                    name = csvFile.name!!,
                    dbData = emptyList<CsvInfo>(),
                  )
                } catch (e: Exception) {
                  return Error(R.string.status_error_import_invalid_data, csvFile.name!!)
                }
              }

              public fun exportRoomToCsv(appExportDirectoryUri: Uri, roomData: RoomData) {
                val appExportDirectory = DocumentFile.fromTreeUri(context, appExportDirectoryUri)!!
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
              }

              private fun exportRoomEntityToCsv(
                newExportDirectory: DocumentFile,
                csvInfo: CsvInfo,
                csvDataList: List<CsvData>,
              ): DocumentFile? {
                val csvFile = newExportDirectory.createFile("text/*", csvInfo.csvFileName) ?:
                  return null // failed to create file
                val outputStream = context.contentResolver.openOutputStream(csvFile.uri) ?:
                  return null // failed to open output stream

                csvWriter().open(outputStream) {
                  writeRow(csvInfo.csvFieldToTypeMap.keys.toList())
                  csvDataList.forEach { writeRow(it.csvRow) }
                }
                return csvFile
              }
            }
        """.trimIndent()
    }
}