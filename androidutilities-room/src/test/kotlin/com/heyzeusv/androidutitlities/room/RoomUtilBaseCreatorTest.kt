package com.heyzeusv.androidutitlities.room

import com.tschuchort.compiletesting.KotlinCompilation
import junit.framework.TestCase.assertTrue
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 *  Used [this article](https://dev.to/chigichan24/why-dont-you-write-unit-tests-and-integration-tests-to-ksp-project-2oio)
 *  as a guide to write these tests.
 */
@OptIn(ExperimentalCompilerApi::class)
class RoomUtilBaseCreatorTest : CreatorTestBase() {

    @Test
    fun `Generate RoomUtilBase`() {
        val kspCompileResult = compile(dummyDb)
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(7, kspCompileResult.generatedFiles.size)
        kspCompileResult.assertFileEquals(expectedRoomUtilBase, "RoomUtilBase.kt")
    }

    @Test
    fun `Do not generate RoomUtilBase when both roomUtilCsv and roomUtilDb options are false`() {
        val kspCompileResult = compile(
            dummyDb,
            kspArguments = mutableMapOf(
                "roomUtilCsv" to "FaLsE",
                "roomUtilDb" to "false",
            ),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(1, kspCompileResult.generatedFiles.size)
        assertFalse(kspCompileResult.generatedFiles.any { it.name == "RoomUtilBase.kt" })
    }


    @Test
    fun `Do generate RoomUtilBase when roomUtilDb option is false`() {
        val kspCompileResult = compile(
            dummyDb,
            kspArguments = mutableMapOf("roomUtilDb" to "false"),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(6, kspCompileResult.generatedFiles.size)
        assertTrue(kspCompileResult.generatedFiles.any { it.name == "RoomUtilBase.kt" })
    }

    @Test
    fun `Do generate RoomUtilBase when roomUtilCsv option is false`() {
        val kspCompileResult = compile(
            dummyDb,
            kspArguments = mutableMapOf("roomUtilCsv" to "false"),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(3, kspCompileResult.generatedFiles.size)
        assertTrue(kspCompileResult.generatedFiles.any { it.name == "RoomUtilBase.kt" })
    }

    companion object {
        val expectedRoomUtilBase = """
            package test

            import android.content.Context
            import android.net.Uri
            import androidx.documentfile.provider.DocumentFile
            import java.lang.UnsupportedOperationException
            import java.text.SimpleDateFormat
            import java.util.Date
            import java.util.Locale
            import kotlin.String
            import kotlinx.coroutines.flow.MutableStateFlow
            import kotlinx.coroutines.flow.StateFlow
            import kotlinx.coroutines.flow.asStateFlow
            import test.RoomUtilStatus.Standby
            
            public abstract class RoomUtilBase(
              private val context: Context,
              private val appDirectoryName: String,
            ) {
              protected val _status: MutableStateFlow<RoomUtilStatus> = MutableStateFlow(Standby)
            
              public val status: StateFlow<RoomUtilStatus> = _status.asStateFlow()
            
              public fun updateStatus(newValue: RoomUtilStatus) {
                _status.value = newValue
              }
            
              protected fun createNewDirectory(appDirectory: DocumentFile): DocumentFile? {
                val sdf = SimpleDateFormat("MMM_dd_yyyy__hh_mm_aa", Locale.getDefault())
                val formattedDate = sdf.format(Date())
                val newDirectory = appDirectory.createDirectory(formattedDate)
                return newDirectory
              }
            
              public fun findOrCreateAppDirectory(selectedDirectoryUri: Uri): Uri? {
                try {
                  val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                  val appDirectory = selectedDirectory.findFile(appDirectoryName) ?:
                    selectedDirectory.createDirectory(appDirectoryName)!!
                    
                  return appDirectory.uri
                } catch (e: UnsupportedOperationException) {
                  // Don't use fromSingleUri(Context, Uri)
                  return null
                }
              }
            }
        """.trimIndent()
    }
}