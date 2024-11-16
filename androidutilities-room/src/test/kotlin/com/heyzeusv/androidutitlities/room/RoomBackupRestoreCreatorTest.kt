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
        assertEquals(7, kspCompileResult.generatedFiles.size)
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
        assertEquals(7, kspCompileResult.generatedFiles.size)
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
        assertEquals(6, kspCompileResult.generatedFiles.size)
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
            import test.RoomUtilStatus.Error
            import test.RoomUtilStatus.Progress
            import test.RoomUtilStatus.Success

            public class RoomBackupRestore(
              private val context: Context,
              private val dbFileName: String,
              private val appDirectoryName: String,
            ) : RoomUtilBase(context, appDirectoryName) {
              init {
                // include file extension to dbFileName!!
              }
              public fun backup(appBackupDirectoryUri: Uri) {
                _status.value = Progress(R.string.backup_progress_started)
                val appBackupDirectory = DocumentFile.fromTreeUri(context, appBackupDirectoryUri)!!
                if (!appBackupDirectory.exists()) {
                  _status.value = Error(R.string.backup_error_missing_directory)
                  return
                }

                val newBackupDirectory = createNewDirectory(appBackupDirectory)
                if (newBackupDirectory == null) {
                  _status.value = Error(R.string.backup_error_create_directory_failed)
                  return
                }
                val dbPath = context.getDatabasePath(dbFileName).path
                val dbFile = DocumentFile.fromFile(File(dbPath))
                val dbWalFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-wal""${'"'}))
                val dbShmFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-shm""${'"'}))
                if (!dbFile.exists()) {
                  _status.value = Error(R.string.backup_error_missing_file, dbPath)
                  return
                }

                val newFiles = mutableListOf(newBackupDirectory)
                val bkpDbFile = newBackupDirectory.createFile("text/*", dbFileName)
                if (bkpDbFile == null) {
                  _status.value = Error(R.string.backup_error_create_file_failed, dbFileName)
                  return
                }
                var dbFileCopyStatus = dbFile.copyTo(bkpDbFile)
                if (!dbFileCopyStatus) {
                  bkpDbFile.delete()
                  newBackupDirectory.delete()
                  _status.value = Error(R.string.backup_error_failed, dbFileName)
                  return
                }
                newFiles.add(bkpDbFile)
                _status.value = Progress(R.string.backup_progress_file_success, dbFileName)

                if (dbWalFile.exists()) {
                  val bkpDbWalFile = newBackupDirectory.createFile("text/*", ""${'"'}${'$'}dbFileName-wal""${'"'})
                  if (bkpDbWalFile == null) {
                    _status.value = Error(R.string.backup_error_create_file_failed, ""${'"'}${'$'}dbFileName-wal""${'"'})
                    return
                  }  
                  dbFileCopyStatus = dbWalFile.copyTo(bkpDbWalFile)
                  if (!dbFileCopyStatus) {
                    newFiles.forEach { it.delete() }
                    bkpDbWalFile.delete()
                    newBackupDirectory.delete()
                    _status.value = Error(R.string.backup_error_failed, ""${'"'}${'$'}dbFileName-wal""${'"'})
                    return
                  }
                  newFiles.add(bkpDbWalFile)
                  _status.value = Progress(R.string.backup_progress_file_success, ""${'"'}${'$'}dbFileName-wal""${'"'})
                }
                    
                if (dbShmFile.exists()) {
                  val bkpDbShmFile = newBackupDirectory.createFile("text/*", ""${'"'}${'$'}dbFileName-shm""${'"'})
                  if (bkpDbShmFile == null) {
                    _status.value = Error(R.string.backup_error_create_file_failed, ""${'"'}${'$'}dbFileName-shm""${'"'})
                    return
                  } 
                  dbFileCopyStatus = dbShmFile.copyTo(bkpDbShmFile)
                  if (!dbFileCopyStatus) {
                    newFiles.forEach { it.delete() }
                    bkpDbShmFile.delete()
                    newBackupDirectory.delete()
                    _status.value = Error(R.string.backup_error_failed, ""${'"'}${'$'}dbFileName-shm""${'"'})
                    return
                  }
                  _status.value = Progress(R.string.backup_progress_file_success, ""${'"'}${'$'}dbFileName-shm""${'"'})
                }
                _status.value = Success(R.string.backup_success)
              }

              public fun restore(selectedDirectoryUri: Uri, restartApp: () -> Unit = { restartAppStandard() }) {
                _status.value = Progress(R.string.restore_progress_started)
                val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  _status.value = Error(R.string.restore_error_missing_directory)
                  return
                }

                val bkpDbFile = selectedDirectory.findFile(dbFileName)
                if (bkpDbFile == null) {
                  _status.value = Error(R.string.restore_error_missing_db_file)
                  return
                }
                val bkpDbWalFile = selectedDirectory.findFile(""${'"'}${'$'}dbFileName-wal""${'"'})
                val bkpDbShmFile = selectedDirectory.findFile(""${'"'}${'$'}dbFileName-shm""${'"'})

                val dbPath = context.getDatabasePath(dbFileName).path
                val dbFile = DocumentFile.fromFile(File(dbPath))
                val dbWalFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-wal""${'"'}))
                val dbShmFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-shm""${'"'}))
                if (!dbFile.exists()) {
                  _status.value = Error(R.string.restore_error_missing_db_file)
                  return
                }

                // delete any existing content before restoring
                PrintWriter(File(dbPath)).close()
                bkpDbFile.copyTo(dbFile)
                _status.value = Progress(R.string.restore_progress_file_success, dbFile.name!!)

                // file doesn't exist in backup so delete current
                if (bkpDbWalFile == null) {
                  dbWalFile.delete()
                } else {
                  // delete any existing content before restoring
                  PrintWriter(File(""${'"'}${'$'}dbPath-wal""${'"'})).close()
                  bkpDbWalFile.copyTo(dbWalFile)
                  _status.value = Progress(R.string.restore_progress_file_success, dbWalFile.name!!)
                }
                // file doesn't exist in backup so delete current
                if (bkpDbShmFile == null) {
                  dbShmFile.delete()
                } else {
                  // delete any existing content before restoring
                  PrintWriter(File(""${'"'}${'$'}dbPath-shm""${'"'})).close()
                  bkpDbShmFile.copyTo(dbShmFile)
                  _status.value = Progress(R.string.restore_progress_file_success, dbShmFile.name!!)
                }

                _status.value = Success(R.string.restore_success)
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
            import test.RoomUtilStatus.Error
            import test.RoomUtilStatus.Progress
            import test.RoomUtilStatus.Success

            public class RoomBackupRestore @Inject constructor(
              private val context: Context,
              private val dbFileName: String,
              private val appDirectoryName: String,
            ) : RoomUtilBase(context, appDirectoryName) {
              init {
                // include file extension to dbFileName!!
              }
              public fun backup(appBackupDirectoryUri: Uri) {
                _status.value = Progress(R.string.backup_progress_started)
                val appBackupDirectory = DocumentFile.fromTreeUri(context, appBackupDirectoryUri)!!
                if (!appBackupDirectory.exists()) {
                  _status.value = Error(R.string.backup_error_missing_directory)
                  return
                }

                val newBackupDirectory = createNewDirectory(appBackupDirectory)
                if (newBackupDirectory == null) {
                  _status.value = Error(R.string.backup_error_create_directory_failed)
                  return
                }
                val dbPath = context.getDatabasePath(dbFileName).path
                val dbFile = DocumentFile.fromFile(File(dbPath))
                val dbWalFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-wal""${'"'}))
                val dbShmFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-shm""${'"'}))
                if (!dbFile.exists()) {
                  _status.value = Error(R.string.backup_error_missing_file, dbPath)
                  return
                }

                val newFiles = mutableListOf(newBackupDirectory)
                val bkpDbFile = newBackupDirectory.createFile("text/*", dbFileName)
                if (bkpDbFile == null) {
                  _status.value = Error(R.string.backup_error_create_file_failed, dbFileName)
                  return
                }
                var dbFileCopyStatus = dbFile.copyTo(bkpDbFile)
                if (!dbFileCopyStatus) {
                  bkpDbFile.delete()
                  newBackupDirectory.delete()
                  _status.value = Error(R.string.backup_error_failed, dbFileName)
                  return
                }
                newFiles.add(bkpDbFile)
                _status.value = Progress(R.string.backup_progress_file_success, dbFileName)

                if (dbWalFile.exists()) {
                  val bkpDbWalFile = newBackupDirectory.createFile("text/*", ""${'"'}${'$'}dbFileName-wal""${'"'})
                  if (bkpDbWalFile == null) {
                    _status.value = Error(R.string.backup_error_create_file_failed, ""${'"'}${'$'}dbFileName-wal""${'"'})
                    return
                  }  
                  dbFileCopyStatus = dbWalFile.copyTo(bkpDbWalFile)
                  if (!dbFileCopyStatus) {
                    newFiles.forEach { it.delete() }
                    bkpDbWalFile.delete()
                    newBackupDirectory.delete()
                    _status.value = Error(R.string.backup_error_failed, ""${'"'}${'$'}dbFileName-wal""${'"'})
                    return
                  }
                  newFiles.add(bkpDbWalFile)
                  _status.value = Progress(R.string.backup_progress_file_success, ""${'"'}${'$'}dbFileName-wal""${'"'})
                }
                    
                if (dbShmFile.exists()) {
                  val bkpDbShmFile = newBackupDirectory.createFile("text/*", ""${'"'}${'$'}dbFileName-shm""${'"'})
                  if (bkpDbShmFile == null) {
                    _status.value = Error(R.string.backup_error_create_file_failed, ""${'"'}${'$'}dbFileName-shm""${'"'})
                    return
                  } 
                  dbFileCopyStatus = dbShmFile.copyTo(bkpDbShmFile)
                  if (!dbFileCopyStatus) {
                    newFiles.forEach { it.delete() }
                    bkpDbShmFile.delete()
                    newBackupDirectory.delete()
                    _status.value = Error(R.string.backup_error_failed, ""${'"'}${'$'}dbFileName-shm""${'"'})
                    return
                  }
                  _status.value = Progress(R.string.backup_progress_file_success, ""${'"'}${'$'}dbFileName-shm""${'"'})
                }
                _status.value = Success(R.string.backup_success)
              }

              public fun restore(selectedDirectoryUri: Uri, restartApp: () -> Unit = { restartAppStandard() }) {
                _status.value = Progress(R.string.restore_progress_started)
                val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  _status.value = Error(R.string.restore_error_missing_directory)
                  return
                }

                val bkpDbFile = selectedDirectory.findFile(dbFileName)
                if (bkpDbFile == null) {
                  _status.value = Error(R.string.restore_error_missing_db_file)
                  return
                }
                val bkpDbWalFile = selectedDirectory.findFile(""${'"'}${'$'}dbFileName-wal""${'"'})
                val bkpDbShmFile = selectedDirectory.findFile(""${'"'}${'$'}dbFileName-shm""${'"'})

                val dbPath = context.getDatabasePath(dbFileName).path
                val dbFile = DocumentFile.fromFile(File(dbPath))
                val dbWalFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-wal""${'"'}))
                val dbShmFile = DocumentFile.fromFile(File(""${'"'}${'$'}dbPath-shm""${'"'}))
                if (!dbFile.exists()) {
                  _status.value = Error(R.string.restore_error_missing_db_file)
                  return
                }

                // delete any existing content before restoring
                PrintWriter(File(dbPath)).close()
                bkpDbFile.copyTo(dbFile)
                _status.value = Progress(R.string.restore_progress_file_success, dbFile.name!!)

                // file doesn't exist in backup so delete current
                if (bkpDbWalFile == null) {
                  dbWalFile.delete()
                } else {
                  // delete any existing content before restoring
                  PrintWriter(File(""${'"'}${'$'}dbPath-wal""${'"'})).close()
                  bkpDbWalFile.copyTo(dbWalFile)
                  _status.value = Progress(R.string.restore_progress_file_success, dbWalFile.name!!)
                }
                // file doesn't exist in backup so delete current
                if (bkpDbShmFile == null) {
                  dbShmFile.delete()
                } else {
                  // delete any existing content before restoring
                  PrintWriter(File(""${'"'}${'$'}dbPath-shm""${'"'})).close()
                  bkpDbShmFile.copyTo(dbShmFile)
                  _status.value = Progress(R.string.restore_progress_file_success, dbShmFile.name!!)
                }

                _status.value = Success(R.string.restore_success)
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