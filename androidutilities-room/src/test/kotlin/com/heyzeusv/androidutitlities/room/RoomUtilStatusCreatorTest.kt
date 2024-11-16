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
class RoomUtilStatusCreatorTest : CreatorTestBase() {

    @Test
    fun `Generate RoomUtilStatus`() {
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
        kspCompileResult.assertFileEquals(expectedRoomUtilStatus, "RoomUtilStatus.kt")
    }

    companion object {
        val expectedRoomUtilStatus = """
            package test
            
            import androidx.`annotation`.StringRes
            import kotlin.Int
            import kotlin.String

            public sealed class RoomUtilStatus {
              public data object Standby : RoomUtilStatus()

              public data class Progress(
                @StringRes
                public val messageId: Int,
                public val name: String = "",
              ) : RoomUtilStatus()

              public data class Error(
                @StringRes
                public val messageId: Int,
                public val name: String = "",
              ) : RoomUtilStatus()

              public data class Success(
                @StringRes
                public val messageId: Int,
              ) : RoomUtilStatus()
            }
        """.trimIndent()
    }
}