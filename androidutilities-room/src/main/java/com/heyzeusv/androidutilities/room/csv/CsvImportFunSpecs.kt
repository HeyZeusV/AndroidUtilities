package com.heyzeusv.androidutilities.room.csv

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.buildCodeBlock

internal fun importCsvToRoomFunSpec(): FunSpec.Builder {
    val selectedDirectoryUri = "selectedDirectoryUri"

    val funSpec = FunSpec.builder("importCsvToRoom")
        .addParameter(selectedDirectoryUri, uriClassName)
        .addCode(buildCodeBlock {
            add("""
                val selectedDirectory = DocumentFile.fromTreeUri(context, $selectedDirectoryUri)!!
                if (!selectedDirectory.exists()) {
                  // selected directory does no exist
                  return null
                }
                val csvDocumentFiles = mutableListOf<DocumentFile>()
                csvFileNames.forEach {
                  val file = selectedDirectory.findFile(it)
                  if (file == null) {
                    // file was not found
                    return null
                  } else {
                    csvDocumentFiles.add(file)
                  }
                }
            """.trimIndent())
        })


    return funSpec
}