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

/**
 *  Used [this article](https://dev.to/chigichan24/why-dont-you-write-unit-tests-and-integration-tests-to-ksp-project-2oio)
 *  as a guide to write these tests.
 */
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
            assertEquals(basicEntityTwoFields("BasicTwoField"), generatedFileText)
        }
    }

    @Test
    fun `Generate two basic entities with two fields`() {
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

                    @Entity
                    class TwoFieldBasic(
                        val basicIntField: Int = 0,
                        val basicStringField: String = "",
                    )
                """
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(2, kspCompileResult.generatedFiles.size)
        kspCompileResult.generatedFiles.forEach {
            println(it.name)
        }
        kspCompileResult.generatedFiles.find { it.name == "BasicTwoFieldRoomUtil.kt" }!!
            .inputStream().use {
                val generatedFileText = String(it.readBytes()).trimIndent()
                assertEquals(basicEntityTwoFields("BasicTwoField"), generatedFileText)
            }
        kspCompileResult.generatedFiles.find { it.name == "TwoFieldBasicRoomUtil.kt" }!!
            .inputStream().use {
                val generatedFileText = String(it.readBytes()).trimIndent()
                assertEquals(basicEntityTwoFields("TwoFieldBasic"), generatedFileText)
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
            assertEquals(
                basicEntityTwoFields("BasicTwoField", "custom_name"),
                generatedFileText
            )
        }
    }

    @Test
    fun `Generate entity with ignored field`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "BasicIgnoredField.kt",
                contents = """
                    package test
                    
                    import androidx.room.Entity
                    import androidx.room.Ignore
                    
                    @Entity
                    class IgnoredField(
                        var basicIntField: Int = 0,
                        var basicStringField: String = "",
                        @Ignore
                        var ignoredLongField: Long = ""
                    )
                """
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(1, kspCompileResult.generatedFiles.size)
        val file = kspCompileResult.generatedFiles[0]
        file.inputStream().use {
            val generatedFileText = String(it.readBytes()).trimIndent()
            assertEquals(basicEntityTwoFields("IgnoredField"), generatedFileText)
        }
    }

    @Test
    fun `Generate entity with two levels of embedded`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "TwoLevelEmbedded.kt",
                contents = """
                    package test
                    
                    import androidx.room.Embedded
                    import androidx.room.Entity

                    @Entity
                    data class TwoLevelEmbedded(
                        val intField: Int = 0,
                        val stringField: String = "",
                        @Embedded
                        val embedOne: EmbedOne = EmbedOne() 
                    )

                    data class EmbedOne(
                        val intFieldOne: Int = 0,
                        val stringFieldOne: String = "",
                        @Embedded
                        val embedTwo: EmbedTwo = EmbedTwo()
                    )

                    data class EmbedTwo(
                        val intFieldTwo: Int = 0,
                        val stringFieldTwo: String = "",
                    )
                """
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(1, kspCompileResult.generatedFiles.size)
        val file = kspCompileResult.generatedFiles[0]
        file.inputStream().use {
            val generatedFileText = String(it.readBytes()).trimIndent()
            assertEquals(expectedTwoLevelEmbedded, generatedFileText)
        }
    }

    @Test
    fun `Generate entity with two levels of embedded with prefixes`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "TwoLevelEmbedded.kt",
                contents = """
                    package test
                    
                    import androidx.room.Embedded
                    import androidx.room.Entity

                    @Entity
                    data class TwoLevelEmbedded(
                        val intField: Int = 0,
                        val stringField: String = "",
                        @Embedded(prefix = "levelOne_")
                        val embedOne: EmbedOne = EmbedOne() 
                    )

                    data class EmbedOne(
                        val intFieldOne: Int = 0,
                        val stringFieldOne: String = "",
                        @Embedded(prefix = "levelTwo_")
                        val embedTwo: EmbedTwo = EmbedTwo()
                    )

                    data class EmbedTwo(
                        val intFieldTwo: Int = 0,
                        val stringFieldTwo: String = "",
                    )
                """
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(1, kspCompileResult.generatedFiles.size)
        val file = kspCompileResult.generatedFiles[0]
        file.inputStream().use {
            val generatedFileText = String(it.readBytes()).trimIndent()
            assertEquals(expectedTwoLevelEmbeddedWithPrefixes, generatedFileText)
        }
    }


    @Test
    fun `Generate entity with two fields with column info`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "TwoFieldColumnInfo.kt",
                contents = """
                    package test
                    
                    import androidx.room.ColumnInfo
                    import androidx.room.Entity
                    
                    @Entity
                    class TwoFieldColumnInfo(
                        @ColumnInfo
                        val intField: Int = 0,
                        @ColumnInfo(name = "customColumnName")
                        val stringField: String = "",
                    )
                """
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(1, kspCompileResult.generatedFiles.size)
        val file = kspCompileResult.generatedFiles[0]
        file.inputStream().use {
            val generatedFileText = String(it.readBytes()).trimIndent()
            assertEquals(expectedEntityWithTwoFieldsWithColumnInfo, generatedFileText)
        }
    }

    @Test
    fun `Do not generate FTS4 entity`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "BasicTwoField.kt",
                contents = """
                    package test
                    
                    import androidx.room.Entity
                    import androidx.room.Fts4
                    
                    @Fts4
                    @Entity
                    class BasicTwoField(
                        val basicIntField: Int = 0,
                        val basicStringField: String = "",
                    )
                """
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(0, kspCompileResult.generatedFiles.size)
    }

    @Test
    fun `Generate entity that requires type converter`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "TypeConverterEntity.kt",
                contents = """
                    package test
                    
                    import androidx.room.Entity
                    import androidx.room.TypeConverter
                    import java.util.Date
                    
                    class TypeConverters {
                        @TypeConverter
                        fun toDate(value: Long): Date {
                            return Date(value)
                        }
                    
                        @TypeConverter
                        fun fromDate(date: Date): Long {
                            return date.time
                        }
                    }

                    @Entity
                    class TypeConverterEntity(val date: Date = 0)
                """
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(1, kspCompileResult.generatedFiles.size)
        val file = kspCompileResult.generatedFiles[0]
        file.inputStream().use {
            val generatedFileText = String(it.readBytes()).trimIndent()
            assertEquals(expectedEntityRequiringTypeConverter, generatedFileText)
        }
    }

    /**
     *  @Entity
     *  @ColumnInfo
     *  @Ignore
     *  @Embedded
     *  @Fts4
     *  @TypeConverter
     */
    @Test
    fun `Generate entity using every scanned annotation`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "AllOptions.kt",
                contents = """
                    package test
                    
                    import androidx.room.ColumnInfo
                    import androidx.room.Embedded
                    import androidx.room.Entity
                    import androidx.room.Fts4
                    import androidx.room.Ignore
                    import androidx.room.TypeConverter
                    import java.util.Date
                    
                    class TypeConverters {
                        @TypeConverter
                        fun toDate(value: Long): Date {
                            return Date(value)
                        }
                    
                        @TypeConverter
                        fun fromDate(date: Date): Long {
                            return date.time
                        }
                    }

                    @Entity(tableName = "custom_name")
                    class AllOptions(
                        @ColumnInfo(name = "customDate")
                        val date: Date = 0,
                        @Ignore
                        val ignoredField: String = "",
                        @Embedded(prefix = "embed_")
                        val embedOne: EmbedOne = EmbedOne(),
                    )

                    data class EmbedOne(
                        val intFieldOne: Int = 0,
                        val stringFieldOne: String = "",
                    )

                    @Fts4
                    @Entity
                    class FtsTable(val basicIntField: Int = 0)
                """
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(1, kspCompileResult.generatedFiles.size)
        val file = kspCompileResult.generatedFiles[0]
        file.inputStream().use {
            val generatedFileText = String(it.readBytes()).trimIndent()
            assertEquals(expectedEntityWithEveryAnnotation, generatedFileText)
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
        private fun basicEntityTwoFields(name: String, tableName: String = name) = """
            package test

            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import kotlin.Any
            import kotlin.Int
            import kotlin.String
            import kotlin.collections.List
            import kotlin.collections.Map

            public data class ${name}RoomUtil(
              public val basicIntField: Int,
              public val basicStringField: String,
            ) : CsvData {
              public val tableName: String = "$tableName"

              override val csvRow: List<Any?> = listOf(
                basicIntField,
                basicStringField,
              )

              public fun toOriginal(): $name = $name(
                basicIntField = basicIntField,
                basicStringField = basicStringField,
              )

              public companion object : CsvInfo {
                override val csvFileName: String = "$tableName.csv"

                override val csvFieldToTypeMap: Map<String, String> = mapOf(
                  "basicIntField" to "Int",
                  "basicStringField" to "String",
                )

                public fun toUtil(entity: $name): ${name}RoomUtil = ${name}RoomUtil(
                  basicIntField = entity.basicIntField,
                  basicStringField = entity.basicStringField,
                )
              }
            }
        """.trimIndent()

        private val expectedTwoLevelEmbedded = """
            package test

            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import kotlin.Any
            import kotlin.Int
            import kotlin.String
            import kotlin.collections.List
            import kotlin.collections.Map

            public data class TwoLevelEmbeddedRoomUtil(
              public val intField: Int,
              public val stringField: String,
              public val intFieldOne: Int,
              public val stringFieldOne: String,
              public val intFieldTwo: Int,
              public val stringFieldTwo: String,
            ) : CsvData {
              public val tableName: String = "TwoLevelEmbedded"

              override val csvRow: List<Any?> = listOf(
                intField,
                stringField,
                intFieldOne,
                stringFieldOne,
                intFieldTwo,
                stringFieldTwo,
              )

              public fun toOriginal(): TwoLevelEmbedded = TwoLevelEmbedded(
                intField = intField,
                stringField = stringField,
                embedOne = EmbedOne(
                  intFieldOne = intFieldOne,
                  stringFieldOne = stringFieldOne,
                  embedTwo = EmbedTwo(
                    intFieldTwo = intFieldTwo,
                    stringFieldTwo = stringFieldTwo,
                  ),
                ),
              )

              public companion object : CsvInfo {
                override val csvFileName: String = "TwoLevelEmbedded.csv"

                override val csvFieldToTypeMap: Map<String, String> = mapOf(
                  "intField" to "Int",
                  "stringField" to "String",
                  "intFieldOne" to "Int",
                  "stringFieldOne" to "String",
                  "intFieldTwo" to "Int",
                  "stringFieldTwo" to "String",
                )

                public fun toUtil(entity: TwoLevelEmbedded): TwoLevelEmbeddedRoomUtil =
                    TwoLevelEmbeddedRoomUtil(
                  intField = entity.intField,
                  stringField = entity.stringField,
                  intFieldOne = entity.embedOne.intFieldOne,
                  stringFieldOne = entity.embedOne.stringFieldOne,
                  intFieldTwo = entity.embedOne.embedTwo.intFieldTwo,
                  stringFieldTwo = entity.embedOne.embedTwo.stringFieldTwo,
                )
              }
            }
        """.trimIndent()

        private val expectedTwoLevelEmbeddedWithPrefixes = """
            package test

            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import kotlin.Any
            import kotlin.Int
            import kotlin.String
            import kotlin.collections.List
            import kotlin.collections.Map

            public data class TwoLevelEmbeddedRoomUtil(
              public val intField: Int,
              public val stringField: String,
              public val levelOne_intFieldOne: Int,
              public val levelOne_stringFieldOne: String,
              public val levelOne_levelTwo_intFieldTwo: Int,
              public val levelOne_levelTwo_stringFieldTwo: String,
            ) : CsvData {
              public val tableName: String = "TwoLevelEmbedded"

              override val csvRow: List<Any?> = listOf(
                intField,
                stringField,
                levelOne_intFieldOne,
                levelOne_stringFieldOne,
                levelOne_levelTwo_intFieldTwo,
                levelOne_levelTwo_stringFieldTwo,
              )

              public fun toOriginal(): TwoLevelEmbedded = TwoLevelEmbedded(
                intField = intField,
                stringField = stringField,
                embedOne = EmbedOne(
                  intFieldOne = levelOne_intFieldOne,
                  stringFieldOne = levelOne_stringFieldOne,
                  embedTwo = EmbedTwo(
                    intFieldTwo = levelOne_levelTwo_intFieldTwo,
                    stringFieldTwo = levelOne_levelTwo_stringFieldTwo,
                  ),
                ),
              )

              public companion object : CsvInfo {
                override val csvFileName: String = "TwoLevelEmbedded.csv"

                override val csvFieldToTypeMap: Map<String, String> = mapOf(
                  "intField" to "Int",
                  "stringField" to "String",
                  "levelOne_intFieldOne" to "Int",
                  "levelOne_stringFieldOne" to "String",
                  "levelOne_levelTwo_intFieldTwo" to "Int",
                  "levelOne_levelTwo_stringFieldTwo" to "String",
                )

                public fun toUtil(entity: TwoLevelEmbedded): TwoLevelEmbeddedRoomUtil =
                    TwoLevelEmbeddedRoomUtil(
                  intField = entity.intField,
                  stringField = entity.stringField,
                  levelOne_intFieldOne = entity.embedOne.intFieldOne,
                  levelOne_stringFieldOne = entity.embedOne.stringFieldOne,
                  levelOne_levelTwo_intFieldTwo = entity.embedOne.embedTwo.intFieldTwo,
                  levelOne_levelTwo_stringFieldTwo = entity.embedOne.embedTwo.stringFieldTwo,
                )
              }
            }
        """.trimIndent()

        private val expectedEntityWithTwoFieldsWithColumnInfo = """
            package test

            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import kotlin.Any
            import kotlin.Int
            import kotlin.String
            import kotlin.collections.List
            import kotlin.collections.Map

            public data class TwoFieldColumnInfoRoomUtil(
              public val intField: Int,
              public val customColumnName: String,
            ) : CsvData {
              public val tableName: String = "TwoFieldColumnInfo"

              override val csvRow: List<Any?> = listOf(
                intField,
                customColumnName,
              )

              public fun toOriginal(): TwoFieldColumnInfo = TwoFieldColumnInfo(
                intField = intField,
                stringField = customColumnName,
              )

              public companion object : CsvInfo {
                override val csvFileName: String = "TwoFieldColumnInfo.csv"

                override val csvFieldToTypeMap: Map<String, String> = mapOf(
                  "intField" to "Int",
                  "customColumnName" to "String",
                )

                public fun toUtil(entity: TwoFieldColumnInfo): TwoFieldColumnInfoRoomUtil =
                    TwoFieldColumnInfoRoomUtil(
                  intField = entity.intField,
                  customColumnName = entity.stringField,
                )
              }
            }
        """.trimIndent()

        private val expectedEntityRequiringTypeConverter = """
            package test

            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import kotlin.Any
            import kotlin.Long
            import kotlin.String
            import kotlin.collections.List
            import kotlin.collections.Map

            public data class TypeConverterEntityRoomUtil(
              public val date: Long,
            ) : CsvData {
              public val tableName: String = "TypeConverterEntity"

              override val csvRow: List<Any?> = listOf(
                date,
              )

              public fun toOriginal(): TypeConverterEntity = TypeConverterEntity(
                date = TypeConverters().toDate(date),
              )

              public companion object : CsvInfo {
                override val csvFileName: String = "TypeConverterEntity.csv"

                override val csvFieldToTypeMap: Map<String, String> = mapOf(
                  "date" to "Long",
                )

                public fun toUtil(entity: TypeConverterEntity): TypeConverterEntityRoomUtil =
                    TypeConverterEntityRoomUtil(
                  date = TypeConverters().fromDate(entity.date),
                )
              }
            }
        """.trimIndent()

        private val expectedEntityWithEveryAnnotation = """
            package test

            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import kotlin.Any
            import kotlin.Int
            import kotlin.Long
            import kotlin.String
            import kotlin.collections.List
            import kotlin.collections.Map

            public data class AllOptionsRoomUtil(
              public val customDate: Long,
              public val embed_intFieldOne: Int,
              public val embed_stringFieldOne: String,
            ) : CsvData {
              public val tableName: String = "custom_name"

              override val csvRow: List<Any?> = listOf(
                customDate,
                embed_intFieldOne,
                embed_stringFieldOne,
              )

              public fun toOriginal(): AllOptions = AllOptions(
                date = TypeConverters().toDate(customDate),
                embedOne = EmbedOne(
                  intFieldOne = embed_intFieldOne,
                  stringFieldOne = embed_stringFieldOne,
                ),
              )

              public companion object : CsvInfo {
                override val csvFileName: String = "custom_name.csv"

                override val csvFieldToTypeMap: Map<String, String> = mapOf(
                  "customDate" to "Long",
                  "embed_intFieldOne" to "Int",
                  "embed_stringFieldOne" to "String",
                )

                public fun toUtil(entity: AllOptions): AllOptionsRoomUtil = AllOptionsRoomUtil(
                  customDate = TypeConverters().fromDate(entity.date),
                  embed_intFieldOne = entity.embedOne.intFieldOne,
                  embed_stringFieldOne = entity.embedOne.stringFieldOne,
                )
              }
            }
        """.trimIndent()
    }
}