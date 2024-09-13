package com.heyzeusv.androidutilities.room.csv

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.heyzeusv.androidutilities.room.CsvInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun exportRoomToCsv(
    context: Context,
    parentDirectoryUri: Uri,
    data: List<List<CsvInfo>>,
) {
    val parentDirectory = DocumentFile.fromTreeUri(context, parentDirectoryUri)!!
    if (!parentDirectory.exists()) {
        // given directory doesn't exist
        return
    } else {
        val newExportDirectory = createNewExportDirectory(parentDirectory)
        if (newExportDirectory == null) {
            // failed to create directory
            return
        } else {
            val newDocumentFiles = mutableListOf<DocumentFile>()
            data.forEach {
//                val csvDocumentFile = exportRoomEntityToCsv(
//                    context = context,
//                    newExportDirectory = newExportDirectory,
//                    entityInfo = it,
//                    entityData = it
//                )
//                newDocumentFiles.add(csvDocumentFile)
            }
        }
    }
}

private fun exportRoomEntityToCsv(
    context: Context,
    newExportDirectory: DocumentFile,
    entityInfo: CsvInfo,
    entityData: List<CsvInfo>,
): DocumentFile {
    // could fail and return null
    val csvDocumentFile = newExportDirectory.createFile("text/*", entityInfo.csvFileName)!!
    // could fail if file is not able to be open or if provider crashed
    val outputStream = context.contentResolver.openOutputStream(csvDocumentFile.uri)!!
    csvWriter().open(outputStream) {
        writeRow(entityInfo.csvHeader)
        entityData.forEach { writeRow(it.csvRow) }
    }
    return csvDocumentFile
}

private fun createNewExportDirectory(parentDirectory: DocumentFile): DocumentFile? {
    val sdf = SimpleDateFormat("MMMM_dd_yyyy__hh_mm_aa", Locale.getDefault())
    val formattedDate = sdf.format(Date())
    val newExportDirectory = parentDirectory.createDirectory(formattedDate)
    return newExportDirectory
}