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
            SourceFile.kotlin(
                name = "TestDatabase.kt",
                contents = """
                    package test

                    import androidx.room.Database

                    @Database
                    abstract class TestDatabase
                """.trimIndent()
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(5, kspCompileResult.generatedFiles.size)
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
            SourceFile.kotlin(
                name = "TestDatabase.kt",
                contents = """
                    package test

                    import androidx.room.Database

                    @Database
                    abstract class TestDatabase
                """.trimIndent()
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(8, kspCompileResult.generatedFiles.size)
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
            SourceFile.kotlin(
                name = "TestDatabase.kt",
                contents = """
                    package test

                    import androidx.room.Database

                    @Database
                    abstract class TestDatabase
                """.trimIndent()
            ),
            kspArguments = mutableMapOf("roomUtilHilt" to "TrUe"),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(5, kspCompileResult.generatedFiles.size)
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
            SourceFile.kotlin(
                name = "TestDatabase.kt",
                contents = """
                    package test

                    import androidx.room.Database

                    @Database
                    abstract class TestDatabase
                """.trimIndent()
            ),
            kspArguments = mutableMapOf("roomUtilCsv" to "FaLsE"),
            )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(1, kspCompileResult.generatedFiles.size)
    }

    companion object {
        val expectedSingleEntityCsvConverter = """
            package test

            import android.content.Context
            import android.net.Uri
            import androidx.documentfile.provider.DocumentFile
            import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
            import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import kotlin.String
            import kotlin.Suppress
            import kotlin.collections.List
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

                val basicTwoFieldRoomUtilData = importCsvToRoomEntity(csvDocumentFiles[0])
                if (basicTwoFieldRoomUtilData == null) return null // error importing data

                return RoomData(
                  basicTwoFieldData =
                    (basicTwoFieldRoomUtilData as List<BasicTwoFieldRoomUtil>).map { it.toOriginal() },
                )
              }

              private fun importCsvToRoomEntity(csvFile: DocumentFile): List<CsvData>? {
                val inputStream = context.contentResolver.openInputStream(csvFile.uri)
                  ?: return null // corrupt file
                try {
                  val content = csvReader().readAll(inputStream)
                  if (content.size == 1) {
                    return emptyList()
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
                  return entityData
                } catch (e: Exception) {
                  return null // invalid data, wrong type data
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
            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import kotlin.String
            import kotlin.Suppress
            import kotlin.collections.List
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

                val entityOneRoomUtilData = importCsvToRoomEntity(csvDocumentFiles[0])
                if (entityOneRoomUtilData == null) return null // error importing data

                val entityTwoRoomUtilData = importCsvToRoomEntity(csvDocumentFiles[1])
                if (entityTwoRoomUtilData == null) return null // error importing data

                val entityThreeRoomUtilData = importCsvToRoomEntity(csvDocumentFiles[2])
                if (entityThreeRoomUtilData == null) return null // error importing data

                val entityFourRoomUtilData = importCsvToRoomEntity(csvDocumentFiles[3])
                if (entityFourRoomUtilData == null) return null // error importing data

                return RoomData(
                  entityOneData = (entityOneRoomUtilData as List<EntityOneRoomUtil>).map { it.toOriginal() },
                  entityTwoData = (entityTwoRoomUtilData as List<EntityTwoRoomUtil>).map { it.toOriginal() },
                  entityThreeData =
                    (entityThreeRoomUtilData as List<EntityThreeRoomUtil>).map { it.toOriginal() },
                  entityFourData = (entityFourRoomUtilData as List<EntityFourRoomUtil>).map { it.toOriginal() },
                )
              }

              private fun importCsvToRoomEntity(csvFile: DocumentFile): List<CsvData>? {
                val inputStream = context.contentResolver.openInputStream(csvFile.uri)
                  ?: return null // corrupt file
                try {
                  val content = csvReader().readAll(inputStream)
                  if (content.size == 1) {
                    return emptyList()
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
                  return entityData
                } catch (e: Exception) {
                  return null // invalid data, wrong type data
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
            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import javax.inject.Inject
            import kotlin.String
            import kotlin.Suppress
            import kotlin.collections.List
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

                val basicTwoFieldRoomUtilData = importCsvToRoomEntity(csvDocumentFiles[0])
                if (basicTwoFieldRoomUtilData == null) return null // error importing data

                return RoomData(
                  basicTwoFieldData =
                    (basicTwoFieldRoomUtilData as List<BasicTwoFieldRoomUtil>).map { it.toOriginal() },
                )
              }

              private fun importCsvToRoomEntity(csvFile: DocumentFile): List<CsvData>? {
                val inputStream = context.contentResolver.openInputStream(csvFile.uri)
                  ?: return null // corrupt file
                try {
                  val content = csvReader().readAll(inputStream)
                  if (content.size == 1) {
                    return emptyList()
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
                  return entityData
                } catch (e: Exception) {
                  return null // invalid data, wrong type data
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