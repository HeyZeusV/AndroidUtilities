package com.heyzeusv.androidutitlities.room

import com.heyzeusv.androidutilities.room.RoomProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals

/**
 *  Used [this article](https://dev.to/chigichan24/why-dont-you-write-unit-tests-and-integration-tests-to-ksp-project-2oio)
 *  as a guide to write these tests.
 */
@OptIn(ExperimentalCompilerApi::class)
abstract class CreatorTestBase {

    @get:Rule
    val tempFolder: TemporaryFolder = TemporaryFolder()

    protected fun compile(
        vararg sourceFiles: SourceFile,
        kspArguments: MutableMap<String, String> = mutableMapOf(),
    ): KspCompileResult {
        kspArguments["roomUtilNamespace"] = "test"
        val compilation = prepareCompilation(*sourceFiles, kspArguments = kspArguments)
        val result = compilation.compile()
        return KspCompileResult(
            result = result,
            generatedFiles = findGeneratedFiles(compilation)
        )
    }

    private fun prepareCompilation(
        vararg sourceFiles: SourceFile,
        kspArguments: MutableMap<String, String>,
    ): KotlinCompilation =
        KotlinCompilation()
            .apply {
                workingDir = tempFolder.root
                inheritClassPath = true
                symbolProcessorProviders = listOf(RoomProcessorProvider())
                sources = sourceFiles.asList()
                verbose = false
                kspArgs = kspArguments
                kspIncremental = true
                messageOutputStream = System.out
            }

    private fun findGeneratedFiles(compilation: KotlinCompilation): List<File> {
        return compilation.kspSourcesDir
            .walkTopDown()
            .filter { it.isFile }
            .toList()
    }

    protected data class KspCompileResult(
        val result: KotlinCompilation.Result,
        val generatedFiles: List<File>,
    ) {
        fun assertFileEquals(expected: String, actualFile: String) {
            generatedFiles.find { it.name == actualFile }!!
                .inputStream().use {
                    val generatedFileText = String(it.readBytes()).trimIndent()
                    assertEquals(expected, generatedFileText)
                }
        }
    }
}