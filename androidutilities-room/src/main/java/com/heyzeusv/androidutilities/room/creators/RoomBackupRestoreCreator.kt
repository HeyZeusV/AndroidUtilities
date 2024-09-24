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

        val classBuilder = TypeSpec.classBuilder(fileName).buildRoomBackupRestore()

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
                addStatement(
                    "val appBackupDirectory = %T.fromTreeUri(%L, %L)!!",
                    documentFileClassName, CONTEXT, appBackupDirectoryUri
                )
                add("""
                    if (!appBackupDirectory.exists()) {
                      return // directory not found
                    }
                    val newBackupDirectory = createNewDirectory(appBackupDirectory) ?:
                      return // new directory could not be created
                    val dbPath = $CONTEXT.getDatabasePath("$DB_FILE_NAME").path
                    
                """.trimIndent())
                addStatement("val dbFile = DocumentFile.fromFile(%T(dbPath))", File::class)
                addStatement("val dbWalFile = DocumentFile.fromFile(File(%P))", "$" + "dbPath-wal")
                addStatement("val dbShmFile = DocumentFile.fromFile(File(%P))", "$" + "dbPath-shm")
                add("""
                    if (!dbFile.exists()) return // main db file not found
                    
                    val newFiles = mutableListOf(newBackupDirectory)
                    val bkpDbFile = newBackupDirectory.createFile("text/*", $DB_FILE_NAME) ?:
                      return // error creating backup file
                    var dbFileCopyStatus = dbFile.copyTo(bkpDbFile)
                    if (!dbFileCopyStatus) {
                      bkpDbFile.delete()
                      newBackupDirectory.delete()
                      return // failed to copy main db file
                    }
                    newFiles.add(bkpDbFile)
                    
                    if (dbWalFile.exists()) {
                    
                """.trimIndent())
                addStatement(
                    "  val bkpDbWalFile = newBackupDirectory.createFile(%S, %P) ?:",
                    "text/*", "$" + "dbFileName-wal"
                )
                add("""
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
                    
                """.trimIndent())
                addStatement(
                    "  val bkpDbShmFile = newBackupDirectory.createFile(%S, %P) ?:",
                    "text/*", "$" + "dbFileName-shm"
                )
                add("""
                        return // error creating backup file
                      dbFileCopyStatus = dbShmFile.copyTo(bkpDbShmFile)
                      if (!dbFileCopyStatus) {
                        newFiles.forEach { it.delete() }
                        bkpDbShmFile.delete()
                        return // failed to copy shm file
                      }
                    }
                """.trimIndent())
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
                    val bkpDbFile = selectedDirectory.findFile($DB_FILE_NAME) ?: return // main db file not found
                    
                """.trimIndent())
                addStatement("val bkpDbWalFile = selectedDirectory.findFile(%P)", "$" + "dbFileName-wal")
                addStatement("val bkpDbShmFile = selectedDirectory.findFile(%P)", "$" + "dbFileName-shm")
                addStatement("")
                add("""val dbPath = context.getDatabasePath("$DB_FILE_NAME").path""")
                addStatement("")
                addStatement("val dbFile = DocumentFile.fromFile(File(dbPath))")
                addStatement("val dbWalFile = DocumentFile.fromFile(File(%P))", "$" + "dbPath-wal")
                addStatement("val dbShmFile = DocumentFile.fromFile(File(%P))", "$" + "dbPath-shm")
                addStatement("if (!dbFile.exists()) return // main db file not found")
                addStatement("")
                addStatement("// delete any existing content before restoring")
                addStatement("%T(File(dbPath)).close()", PrintWriter::class)
                add("""
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
                """.trimIndent(),  "$" + "dbPath-wal", "$" + "dbPath-shm")
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
                    
                """.trimIndent())
                addStatement("} catch (e: %T) {", FileNotFoundException::class)
                add("""
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