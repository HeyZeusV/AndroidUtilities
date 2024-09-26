package com.heyzeusv.androidutilities.room.creators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.util.getPackageName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter

private const val CONTEXT = "context"
private const val DB_FILE_NAME = "dbFileName"
private const val TEXT_MIME = "text/*"

internal class RoomBackupRestoreCreator(
    private val codeGenerator: CodeGenerator,
    private val dbClassDeclaration: KSClassDeclaration,
    private val logger: KSPLogger,
) {
    private val documentFileClassName = ClassName("androidx.documentfile.provider", "DocumentFile")
    private val uriClassName = ClassName("android.net", "Uri")

    private fun createRoomBackupRestoreFile() {
        logger.info("Creating RoomBackupRestore...")
        val packageName = dbClassDeclaration.getPackageName()
        val fileName = "RoomBackupRestore"
        val fileBuilder = FileSpec.builder(packageName, fileName)

        val classBuilder = TypeSpec.classBuilder(fileName)
            .superclass(ClassName(packageName, "RoomUtilBase"))
            .buildRoomBackupRestore()

        fileBuilder.addType(classBuilder.build())

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dbClassDeclaration.containingFile!!),
            packageName = packageName,
            fileName = fileName,
            extensionName = "kt"
        ).bufferedWriter().use { fileBuilder.build().writeTo(it) }
    }

    private fun TypeSpec.Builder.buildRoomBackupRestore(): TypeSpec.Builder {
        val contextClassName = ClassName("android.content", "Context")
        // context parameter/property in order to read/write files
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addComment("include file extension to dbFileName!!")
                .addParameter(CONTEXT, contextClassName)
                .addParameter(DB_FILE_NAME, String::class)
                .build()
        )
        addProperty(
            PropertySpec.builder(CONTEXT, contextClassName)
                .initializer(CONTEXT)
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        addProperty(
            PropertySpec.builder(DB_FILE_NAME, String::class)
                .initializer(DB_FILE_NAME)
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        addFunction(buildBackupFunction().build())
        addFunction(buildRestoreFunction().build())
        addFunction(buildCopyToFunction().build())
        addFunction(buildCloseAppFunction().build())

        return this
    }

    private fun buildBackupFunction(): FunSpec.Builder {
        val appBackupDirectoryUri = "appBackupDirectoryUri"

        val funSpec = FunSpec.builder("backup")
            .addParameter(appBackupDirectoryUri, uriClassName)
            .addCode(buildCodeBlock {
                add("""
                    val appBackupDirectory = %T.fromTreeUri(%L, %L)!!
                    if (!appBackupDirectory.exists()) {
                      return // directory not found
                    }
                    
                """.trimIndent(), documentFileClassName, CONTEXT, appBackupDirectoryUri)
                add("""
                    
                    val newBackupDirectory = createNewDirectory(appBackupDirectory) ?:
                      return // new directory could not be created
                    val dbPath = %L.getDatabasePath(%L).path
                    
                """.trimIndent(), CONTEXT, DB_FILE_NAME)
                add("""
                    val dbFile = DocumentFile.fromFile(%T(dbPath))
                    val dbWalFile = DocumentFile.fromFile(File(%P))
                    val dbShmFile = DocumentFile.fromFile(File(%P))
                    if (!dbFile.exists()) return // main db file not found
                    
                """.trimIndent(), File::class, "$" + "dbPath-wal", "$" + "dbPath-shm")
                add("""
                    
                    val newFiles = mutableListOf(newBackupDirectory)
                    val bkpDbFile = newBackupDirectory.createFile(%S, %L) ?:
                      return // error creating backup file
                    var dbFileCopyStatus = dbFile.copyTo(bkpDbFile)
                    if (!dbFileCopyStatus) {
                      bkpDbFile.delete()
                      newBackupDirectory.delete()
                      return // failed to copy main db file
                    }
                    newFiles.add(bkpDbFile)
                    
                """.trimIndent(), TEXT_MIME, DB_FILE_NAME)
                add("""
                    
                    if (dbWalFile.exists()) {
                      val bkpDbWalFile = newBackupDirectory.createFile(%S, %P) ?:
                        return // error creating backup file
                      dbFileCopyStatus = dbWalFile.copyTo(bkpDbWalFile)
                      if (!dbFileCopyStatus) {
                        newFiles.forEach { it.delete() }
                        bkpDbWalFile.delete()
                        return // failed to copy wal file
                      }
                      newFiles.add(bkpDbWalFile)
                    }
                        
                """.trimIndent(), TEXT_MIME, "$" + "dbFileName-wal")
                add("""
                    
                    if (dbShmFile.exists()) {
                      val bkpDbShmFile = newBackupDirectory.createFile(%S, %P) ?:
                        return // error creating backup file
                      dbFileCopyStatus = dbShmFile.copyTo(bkpDbShmFile)
                      if (!dbFileCopyStatus) {
                        newFiles.forEach { it.delete() }
                        bkpDbShmFile.delete()
                        return // failed to copy shm file
                      }
                    }
                """.trimIndent(), TEXT_MIME, "$" + "dbFileName-shm")
            })

        return funSpec
    }

    private fun buildRestoreFunction(): FunSpec.Builder {
        val unitLambda = LambdaTypeName.get(returnType = Unit::class.asTypeName())
        val funSpec = FunSpec.builder("restore")
            .addParameter("selectedDirectoryUri", uriClassName)
            .addParameter(ParameterSpec.builder("restartApp", unitLambda)
                .defaultValue("{ restartAppStandard() }")
                .build()
            )
            .addCode(buildCodeBlock {
                add("""
                    val selectedDirectory = DocumentFile.fromTreeUri(context, selectedDirectoryUri)!!
                    if (!selectedDirectory.exists()) {
                      return // directory doesn't exist
                    }
                    
                """.trimIndent())
                add("""
                    
                    val bkpDbFile = selectedDirectory.findFile(%L) ?: return // main db file not found
                    val bkpDbWalFile = selectedDirectory.findFile(%P)
                    val bkpDbShmFile = selectedDirectory.findFile(%P)
                    
                """.trimIndent(), DB_FILE_NAME, "$" + "dbFileName-wal", "$" + "dbFileName-shm")
                add("""
                    
                    val dbPath = context.getDatabasePath(%L).path
                    val dbFile = DocumentFile.fromFile(File(dbPath))
                    val dbWalFile = DocumentFile.fromFile(File(%P))
                    val dbShmFile = DocumentFile.fromFile(File(%P))
                    if (!dbFile.exists()) return // main db file not found
                    
                """.trimIndent(), DB_FILE_NAME, "$" + "dbPath-wal", "$" + "dbPath-shm")
                add("""
                    
                    // delete any existing content before restoring
                    %T(File(dbPath)).close()
                    bkpDbFile.copyTo(dbFile)
                    // file doesn't exist in backup so delete current
                    if (bkpDbWalFile == null) {
                      dbWalFile.delete()
                    } else {
                      // delete any existing content before restoring
                      PrintWriter(File(%P)).close()
                      bkpDbWalFile.copyTo(dbWalFile)
                    }
                    // file doesn't exist in backup so delete current
                    if (bkpDbShmFile == null) {
                      dbShmFile.delete()
                    } else {
                      // delete any existing content before restoring
                      PrintWriter(File(%P)).close()
                      bkpDbShmFile.copyTo(dbShmFile)
                    }
                    
                    restartApp()
                """.trimIndent(),  PrintWriter::class, "$" + "dbPath-wal", "$" + "dbPath-shm")
            })

        return funSpec
    }

    private fun buildCopyToFunction(): FunSpec.Builder {
        val funSpec = FunSpec.builder("copyTo")
            .receiver(documentFileClassName)
            .returns(Boolean::class)
            .addModifiers(KModifier.PRIVATE)
            .addParameter("targetFile", documentFileClassName)
            .addCode(buildCodeBlock {
                add("""
                    val input = try {
                      // returns false if fails to open stream
                      context.contentResolver.openInputStream(this.uri) ?: return false
                    } catch (e: %T) {
                      return false
                    }
                    val output = try {
                      // returns false if fails to open stream
                      context.contentResolver.openOutputStream(targetFile.uri) ?: return false
                    } catch (e: FileNotFoundException) {
                      return false
                    }
                    
                """.trimIndent(), FileNotFoundException::class)
                add("""
                    
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
                """.trimIndent())
            })

        return funSpec
    }

    private fun buildCloseAppFunction(): FunSpec.Builder {
        val intentClassName = ClassName("android.content", "Intent")
        val funSpec = FunSpec.builder("restartAppStandard")
            .addModifiers(KModifier.PRIVATE)
            .addCode(buildCodeBlock {
                add("""
                    val packageManager = context.packageManager
                    val intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
                    val componentName = intent.component!!
                    val restartIntent = %T.makeRestartActivityTask(componentName)
                    context.startActivity(restartIntent)
                    %T.getRuntime().exit(0)
                """.trimIndent(), intentClassName, Runtime::class)
            })

        return funSpec
    }

    init {
        createRoomBackupRestoreFile()
    }
}