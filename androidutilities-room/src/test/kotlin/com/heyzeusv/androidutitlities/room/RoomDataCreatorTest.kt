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
class RoomDataCreatorTest : CreatorTestBase() {

    @Test
    fun `Generate RoomData for single entity database`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "BasicTwoField.kt",
                contents = """
                    package test.entity
                    
                    import androidx.room.Entity
                    
                    @Entity
                    class BasicTwoField(
                        val basicIntField: Int = 0,
                        val basicStringField: String = "",
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
        kspCompileResult.assertFileEquals(expectedSingleEntityRoomData, "RoomData.kt")
    }

    @Test
    fun `Generate RoomData for multiple entity database`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "Entities.kt",
                contents = """
                    package test.entity
                    
                    import androidx.room.Entity
                    
                    @Entity
                    class EntityOne(
                        val basicIntField: Int = 0,
                        val basicStringField: String = "",
                    )

                    @Entity
                    class EntityTwo(
                        val basicIntField: Int = 0,
                        val basicStringField: String = "",
                    )

                    @Entity
                    class EntityThree(
                        val basicIntField: Int = 0,
                        val basicStringField: String = "",
                    )

                    @Entity
                    class EntityFour(
                        val basicIntField: Int = 0,
                        val basicStringField: String = "",
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
        kspCompileResult.assertFileEquals(expectedMultipleEntityRoomData, "RoomData.kt")
    }

    companion object {
        private val expectedSingleEntityRoomData = """
            package test

            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import kotlin.collections.List
            import kotlin.collections.Map
            import test.entity.BasicTwoField
            import test.entity.BasicTwoFieldRoomUtil

            public data class RoomData(
              public val basicTwoFieldData: List<BasicTwoField> = emptyList(),
            ) {
              private val basicTwoFieldRoomUtilData: List<BasicTwoFieldRoomUtil> = basicTwoFieldData.map {
                BasicTwoFieldRoomUtil.toUtil(it)
              }

              public val csvDataMap: Map<CsvInfo, List<CsvData>> = mapOf(
                BasicTwoFieldRoomUtil to basicTwoFieldRoomUtilData,
              )
            }
        """.trimIndent()

        private val expectedMultipleEntityRoomData = """
            package test
            
            import com.heyzeusv.androidutilities.room.util.CsvData
            import com.heyzeusv.androidutilities.room.util.CsvInfo
            import kotlin.collections.List
            import kotlin.collections.Map
            import test.entity.EntityFour
            import test.entity.EntityFourRoomUtil
            import test.entity.EntityOne
            import test.entity.EntityOneRoomUtil
            import test.entity.EntityThree
            import test.entity.EntityThreeRoomUtil
            import test.entity.EntityTwo
            import test.entity.EntityTwoRoomUtil
            
            public data class RoomData(
              public val entityOneData: List<EntityOne> = emptyList(),
              public val entityTwoData: List<EntityTwo> = emptyList(),
              public val entityThreeData: List<EntityThree> = emptyList(),
              public val entityFourData: List<EntityFour> = emptyList(),
            ) {
              private val entityOneRoomUtilData: List<EntityOneRoomUtil> = entityOneData.map {
                EntityOneRoomUtil.toUtil(it)
              }
            
              private val entityTwoRoomUtilData: List<EntityTwoRoomUtil> = entityTwoData.map {
                EntityTwoRoomUtil.toUtil(it)
              }
            
              private val entityThreeRoomUtilData: List<EntityThreeRoomUtil> = entityThreeData.map {
                EntityThreeRoomUtil.toUtil(it)
              }
            
              private val entityFourRoomUtilData: List<EntityFourRoomUtil> = entityFourData.map {
                EntityFourRoomUtil.toUtil(it)
              }
            
              public val csvDataMap: Map<CsvInfo, List<CsvData>> = mapOf(
                EntityOneRoomUtil to entityOneRoomUtilData,
                EntityTwoRoomUtil to entityTwoRoomUtilData,
                EntityThreeRoomUtil to entityThreeRoomUtilData,
                EntityFourRoomUtil to entityFourRoomUtilData,
              )
            }
        """.trimIndent()
    }
}