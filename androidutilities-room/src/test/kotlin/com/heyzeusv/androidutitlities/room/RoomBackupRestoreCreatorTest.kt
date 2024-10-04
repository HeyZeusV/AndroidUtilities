package com.heyzeusv.androidutitlities.room

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 *  Used [this article](https://dev.to/chigichan24/why-dont-you-write-unit-tests-and-integration-tests-to-ksp-project-2oio)
 *  as a guide to write these tests.
 */
@OptIn(ExperimentalCompilerApi::class)
class RoomBackupRestoreCreatorTest : CreatorTestBase() {

    @Test
    fun `Generate RoomBackupRestore`() {
        val kspCompileResult = compile(
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
        assertEquals(6, kspCompileResult.generatedFiles.size)
        kspCompileResult.assertFileEquals(expectedRoomBackupRestore, "RoomBackupRestore.kt")
    }

    @Test
    fun `Generate RoomBackupRestore with Hilt inject when roomUtilHilt option is true`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "TestDatabase.kt",
                contents = """
                    package test

                    import androidx.room.Database

                    @Database
                    abstract class TestDatabase
                """.trimIndent()
            ),
            kspArguments = mutableMapOf("roomUtilHilt" to "TRUE"),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(6, kspCompileResult.generatedFiles.size)
        kspCompileResult.assertFileEquals(
            expectedRoomBackupRestoreWithHiltOptionValue,
            "RoomBackupRestore.kt",
        )
    }


    @Test
    fun `Do not generate RoomBackupRestore when roomUtilDb option is false`() {
        val kspCompileResult = compile(
            SourceFile.kotlin(
                name = "TestDatabase.kt",
                contents = """
                    package test

                    import androidx.room.Database

                    @Database
                    abstract class TestDatabase
                """.trimIndent()
            ),
            kspArguments = mutableMapOf("roomUtilDb" to "FALSE"),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, kspCompileResult.result.exitCode)
        assertEquals(5, kspCompileResult.generatedFiles.size)
        assertFalse(kspCompileResult.generatedFiles.any { it.name == "RoomBackupRestore.kt" })
    }


    companion object {
        val expectedRoomBackupRestore = """
            package test

            import android.content.Context
            import android.content.Intent
            import android.net.Uri
            import androidx.documentfile.provider.DocumentFile
            import java.io.File
            import java.io.FileNotFoundException
            import java.io.PrintWriter
            import java.lang.Runtime
            import kotlin.Boolean
            import kotlin.String
            import kotlin.Unit

            public class RoomBackupRestore(
              private val context: Context,
              private val dbFileName: String,
              private val appDirectoryName: String,
            ) : RoomUtilBase(context, appDirectoryName) {
              init {
                // include file extension to dbFileName!!
              }
              public fun backup(appBackupDirectoryUri: Uri) {
                val appBackupDirectory = DocumentFile.fromTreeUri(context, appBackupDirectoryUri)!!
                if (!appBackupDirectory.exists()) {
                  return // directory not found
                }

                val newBackupDirectory = createNewDirectory(appBackupDirectory) ?:
                  return // new directory could not be created
                val dbPath = context.getDatabasePath(dbFileName).path
                val dbFile = DocumentFile.fromFile(File(dbPath))
                val dbWalFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-wal""${'"'}))
                val dbShmFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-shm""${'"'}))
                if (!dbFile.exists()) return // main db file not found

                val newFiles = mutableListOf(newBackupDirectory)
                val bkpDbFile = newBackupDirectory.createFile("text/*", dbFileName) ?:
                  return // error creating backup file
                var dbFileCopyStatus = dbFile.copyTo(bkpDbFile)
                if (!dbFileCopyStatus) {
                  bkpDbFile.delete()
                  newBackupDirectory.delete()
                  return // failed to copy main db file
                }
                newFiles.add(bkpDbFile)

                if (dbWalFile.exists()) {
                  val bkpDbWalFile = newBackupDirectory.createFile("text/*", ""${'"'}${'$'}dbFileName-wal""${'"'}) ?:
                    return // error creating backup file
                  dbFileCopyStatus = dbWalFile.copyTo(bkpDbWalFile)
                  if (!dbFileCopyStatus) {
                    newFiles.forEach { it.delete() }
                    bkpDbWalFile.delete()
                    return // failed to copy wal file
                  }
                  newFiles.add(bkpDbWalFile)
                }
                    
                if (dbShmFile.exists()) {
                  val bkpDbShmFile = newBackupDirectory.createFile("text/*", ""${'"'}${'$'}dbFileName-shm""${'"'}) ?:
                    return // error creating backup file
                  dbFileCopyStatus = dbShmFile.copyTo(bkpDbShmFile)
                  if (!dbFileCopyStatus) {
                    newFiles.forEach { it.delete() }
                    bkpDbShmFile.delete()
                    return // failed to copy shm file
                  }
                }
              }

              public fun restore(selectedDirectoryUri: Uri, restartApp: () -> Unit = { restartAppStandard() }) {
                val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  return // directory doesn't exist
                }

                val bkpDbFile = selectedDirectory.findFile(dbFileName) ?: return // main db file not found
                val bkpDbWalFile = selectedDirectory.findFile(""${'"'}${'$'}dbFileName-wal""${'"'})
                val bkpDbShmFile = selectedDirectory.findFile(""${'"'}${'$'}dbFileName-shm""${'"'})

                val dbPath = context.getDatabasePath(dbFileName).path
                val dbFile = DocumentFile.fromFile(File(dbPath))
                val dbWalFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-wal""${'"'}))
                val dbShmFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-shm""${'"'}))
                if (!dbFile.exists()) return // main db file not found

                // delete any existing content before restoring
                PrintWriter(File(dbPath)).close()
                bkpDbFile.copyTo(dbFile)
                // file doesn't exist in backup so delete current
                if (bkpDbWalFile == null) {
                  dbWalFile.delete()
                } else {
                  // delete any existing content before restoring
                  PrintWriter(File(""${'"'}${'$'}dbPath-wal""${'"'})).close()
                  bkpDbWalFile.copyTo(dbWalFile)
                }
                // file doesn't exist in backup so delete current
                if (bkpDbShmFile == null) {
                  dbShmFile.delete()
                } else {
                  // delete any existing content before restoring
                  PrintWriter(File(""${'"'}${'$'}dbPath-shm""${'"'})).close()
                  bkpDbShmFile.copyTo(dbShmFile)
                }

                restartApp()
              }

              private fun DocumentFile.copyTo(targetFile: DocumentFile): Boolean {
                val input = try {
                  // returns false if fails to open stream
                  context.contentResolver.openInputStream(this.uri) ?: return false
                } catch (e: FileNotFoundException) {
                  return false
                }
                val output = try {
                  // returns false if fails to open stream
                  context.contentResolver.openOutputStream(targetFile.uri) ?: return false
                } catch (e: FileNotFoundException) {
                  return false
                }

                try {
                  val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                  var read = input.read(buffer)
                  while (read != -1) {
                    output.write(buffer, 0, read)
                    read = input.read(buffer)
                  }
                } catch (e: Exception) {
                  return false // export failed
                } finally {
                  input.close()
                  output.flush()
                  output.close()
                }
                return true
              }

              private fun restartAppStandard() {
                val packageManager = context.packageManager
                val intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
                val componentName = intent.component!!
                val restartIntent = Intent.makeRestartActivityTask(componentName)
                context.startActivity(restartIntent)
                Runtime.getRuntime().exit(0)
              }
            }
        """.trimIndent()

        val expectedRoomBackupRestoreWithHiltOptionValue = """
            package test

            import android.content.Context
            import android.content.Intent
            import android.net.Uri
            import androidx.documentfile.provider.DocumentFile
            import java.io.File
            import java.io.FileNotFoundException
            import java.io.PrintWriter
            import java.lang.Runtime
            import javax.inject.Inject
            import kotlin.Boolean
            import kotlin.String
            import kotlin.Unit

            public class RoomBackupRestore @Inject constructor(
              private val context: Context,
              private val dbFileName: String,
              private val appDirectoryName: String,
            ) : RoomUtilBase(context, appDirectoryName) {
              init {
                // include file extension to dbFileName!!
              }
              public fun backup(appBackupDirectoryUri: Uri) {
                val appBackupDirectory = DocumentFile.fromTreeUri(context, appBackupDirectoryUri)!!
                if (!appBackupDirectory.exists()) {
                  return // directory not found
                }

                val newBackupDirectory = createNewDirectory(appBackupDirectory) ?:
                  return // new directory could not be created
                val dbPath = context.getDatabasePath(dbFileName).path
                val dbFile = DocumentFile.fromFile(File(dbPath))
                val dbWalFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-wal""${'"'}))
                val dbShmFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-shm""${'"'}))
                if (!dbFile.exists()) return // main db file not found

                val newFiles = mutableListOf(newBackupDirectory)
                val bkpDbFile = newBackupDirectory.createFile("text/*", dbFileName) ?:
                  return // error creating backup file
                var dbFileCopyStatus = dbFile.copyTo(bkpDbFile)
                if (!dbFileCopyStatus) {
                  bkpDbFile.delete()
                  newBackupDirectory.delete()
                  return // failed to copy main db file
                }
                newFiles.add(bkpDbFile)

                if (dbWalFile.exists()) {
                  val bkpDbWalFile = newBackupDirectory.createFile("text/*", ""${'"'}${'$'}dbFileName-wal""${'"'}) ?:
                    return // error creating backup file
                  dbFileCopyStatus = dbWalFile.copyTo(bkpDbWalFile)
                  if (!dbFileCopyStatus) {
                    newFiles.forEach { it.delete() }
                    bkpDbWalFile.delete()
                    return // failed to copy wal file
                  }
                  newFiles.add(bkpDbWalFile)
                }
                    
                if (dbShmFile.exists()) {
                  val bkpDbShmFile = newBackupDirectory.createFile("text/*", ""${'"'}${'$'}dbFileName-shm""${'"'}) ?:
                    return // error creating backup file
                  dbFileCopyStatus = dbShmFile.copyTo(bkpDbShmFile)
                  if (!dbFileCopyStatus) {
                    newFiles.forEach { it.delete() }
                    bkpDbShmFile.delete()
                    return // failed to copy shm file
                  }
                }
              }

              public fun restore(selectedDirectoryUri: Uri, restartApp: () -> Unit = { restartAppStandard() }) {
                val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  return // directory doesn't exist
                }

                val bkpDbFile = selectedDirectory.findFile(dbFileName) ?: return // main db file not found
                val bkpDbWalFile = selectedDirectory.findFile(""${'"'}${'$'}dbFileName-wal""${'"'})
                val bkpDbShmFile = selectedDirectory.findFile(""${'"'}${'$'}dbFileName-shm""${'"'})

                val dbPath = context.getDatabasePath(dbFileName).path
                val dbFile = DocumentFile.fromFile(File(dbPath))
                val dbWalFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-wal""${'"'}))
                val dbShmFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-shm""${'"'}))
                if (!dbFile.exists()) return // main db file not found

                // delete any existing content before restoring
                PrintWriter(File(dbPath)).close()
                bkpDbFile.copyTo(dbFile)
                // file doesn't exist in backup so delete current
                if (bkpDbWalFile == null) {
                  dbWalFile.delete()
                } else {
                  // delete any existing content before restoring
                  PrintWriter(File(""${'"'}${'$'}dbPath-wal""${'"'})).close()
                  bkpDbWalFile.copyTo(dbWalFile)
                }
                // file doesn't exist in backup so delete current
                if (bkpDbShmFile == null) {
                  dbShmFile.delete()
                } else {
                  // delete any existing content before restoring
                  PrintWriter(File(""${'"'}${'$'}dbPath-shm""${'"'})).close()
                  bkpDbShmFile.copyTo(dbShmFile)
                }

                restartApp()
              }

              private fun DocumentFile.copyTo(targetFile: DocumentFile): Boolean {
                val input = try {
                  // returns false if fails to open stream
                  context.contentResolver.openInputStream(this.uri) ?: return false
                } catch (e: FileNotFoundException) {
                  return false
                }
                val output = try {
                  // returns false if fails to open stream
                  context.contentResolver.openOutputStream(targetFile.uri) ?: return false
                } catch (e: FileNotFoundException) {
                  return false
                }

                try {
                  val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                  var read = input.read(buffer)
                  while (read != -1) {
                    output.write(buffer, 0, read)
                    read = input.read(buffer)
                  }
                } catch (e: Exception) {
                  return false // export failed
                } finally {
                  input.close()
                  output.flush()
                  output.close()
                }
                return true
              }

              private fun restartAppStandard() {
                val packageManager = context.packageManager
                val intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
                val componentName = intent.component!!
                val restartIntent = Intent.makeRestartActivityTask(componentName)
                context.startActivity(restartIntent)
                Runtime.getRuntime().exit(0)
              }
            }
        """.trimIndent()
    }
}