package com.heyzeusv.androidutitlities.room

import com.heyzeusv.androidutilities.room.RoomProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals

@OptIn(ExperimentalCompilerApi::class)
class RoomProcessorTest  {

    @get:Rule
    val tempFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `Generate basic entity with two fields`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "BasicTwoField.kt",
                contents = """
                    package test
                    
                    import androidx.room.Entity
                    
                    @Entity
                    class BasicTwoField(
                        val basicIntField: Int = 0,
                        val basicStringField: String = "",
                    )
                """
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(1, kspCompileResult.generatedFiles.size)
        val file = kspCompileResult.generatedFiles[0]
        file.inputStream().use {
            val generatedFileText = String(it.readBytes()).trimIndent()
            assertEquals(expectedBasicEntityWithTwoFields, generatedFileText)
        }
    }

    @Test
    fun `Generate entity with custom table name`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "BasicTwoField.kt",
                contents = """
                    package test
                    
                    import androidx.room.Entity
                    
                    @Entity(tableName = "custom_name")
                    class BasicTwoField(
                        val basicIntField: Int = 0,
                        val basicStringField: String = "",
                    )
                """
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(1, kspCompileResult.generatedFiles.size)
        val file = kspCompileResult.generatedFiles[0]
        file.inputStream().use {
            val generatedFileText = String(it.readBytes()).trimIndent()
            assertEquals(expectedBasicEntityWithCustomName, generatedFileText)
        }
    }

    private fun compile(vararg sourceFiles: SourceFile): KspCompileResult {
        val compilation = prepareCompilation(*sourceFiles)
        val result = compilation.compile()
        return KspCompileResult(
            result = result,
            generatedFiles = findGeneratedFiles(compilation)
        )
    }

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation =
        KotlinCompilation()
            .apply {
                workingDir = tempFolder.root
                inheritClassPath = true
                symbolProcessorProviders = listOf(RoomProcessorProvider())
                sources = sourceFiles.asList()
                verbose = false
                kspIncremental = true
                messageOutputStream = System.out
            }

    private fun findGeneratedFiles(compilation: KotlinCompilation): List<File> {
        val list = compilation.kspSourcesDir.listFiles()
        list?.forEach {
            println("name ${it.name}, isFile ${it.isFile}")
        }
        print("${compilation.kspSourcesDir.listFiles()?.size}")
        return compilation.kspSourcesDir
            .walkTopDown()
            .filter { it.isFile }
            .toList()
    }

    private data class KspCompileResult(
        val result: KotlinCompilation.Result,
        val generatedFiles: List<File>,
    )

    companion object {
        private val expectedBasicEntityWithTwoFields = """
            package test

            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import kotlin.Any
            import kotlin.Int
            import kotlin.String
            import kotlin.collections.List
            import kotlin.collections.Map

            public data class BasicTwoFieldRoomUtil(
              public val basicIntField: Int,
              public val basicStringField: String,
            ) : CsvData {
              public val tableName: String = "BasicTwoField"

              override val csvRow: List<Any?> = listOf(
                basicIntField,
                basicStringField,
              )

              public fun toOriginal(): BasicTwoField = BasicTwoField(
                basicIntField = basicIntField,
                basicStringField = basicStringField,
              )

              public companion object : CsvInfo {
                override val csvFileName: String = "BasicTwoField.csv"

                override val csvFieldToTypeMap: Map<String, String> = mapOf(
                  "basicIntField" to "Int",
                  "basicStringField" to "String",
                )

                public fun toUtil(entity: BasicTwoField): BasicTwoFieldRoomUtil = BasicTwoFieldRoomUtil(
                  basicIntField = entity.basicIntField,
                  basicStringField = entity.basicStringField,
                )
              }
            }
        """.trimIndent()

        private val expectedBasicEntityWithCustomName = """
            package test

            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import kotlin.Any
            import kotlin.Int
            import kotlin.String
            import kotlin.collections.List
            import kotlin.collections.Map

            public data class BasicTwoFieldRoomUtil(
              public val basicIntField: Int,
              public val basicStringField: String,
            ) : CsvData {
              public val tableName: String = "custom_name"

              override val csvRow: List<Any?> = listOf(
                basicIntField,
                basicStringField,
              )

              public fun toOriginal(): BasicTwoField = BasicTwoField(
                basicIntField = basicIntField,
                basicStringField = basicStringField,
              )

              public companion object : CsvInfo {
                override val csvFileName: String = "custom_name.csv"

                override val csvFieldToTypeMap: Map<String, String> = mapOf(
                  "basicIntField" to "Int",
                  "basicStringField" to "String",
                )

                public fun toUtil(entity: BasicTwoField): BasicTwoFieldRoomUtil = BasicTwoFieldRoomUtil(
                  basicIntField = entity.basicIntField,
                  basicStringField = entity.basicStringField,
                )
              }
            }
        """.trimIndent()
    }
}