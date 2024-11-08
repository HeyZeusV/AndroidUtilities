package com.heyzeusv.androidutitlities.room

import com.tschuchort.compiletesting.KotlinCompilation
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import kotlin.test.assertEquals

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