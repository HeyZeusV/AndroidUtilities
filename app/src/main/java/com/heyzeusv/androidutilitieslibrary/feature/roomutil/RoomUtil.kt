package com.heyzeusv.androidutilitieslibrary.feature.roomutil

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun RoomUtilScreen(
    roomUtilVM: RoomUtilViewModel,
) {
    val context = LocalContext.current

    val dbRestoreLauncher = rememberLauncherForActivityResult(contract = OpenDocumentTree()) {
        it?.let { uri ->
            val flags = FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            roomUtilVM.restoreDatabase(uri)
        }
    }
    val dbBackupLauncher = rememberLauncherForActivityResult(contract = OpenDocumentTree()) {
        it?.let { uri ->
            val flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            roomUtilVM.setupAppDirectoryAndBackupDatabase(uri)
        }
    }
    val csvImportLauncher = rememberLauncherForActivityResult(contract = OpenDocumentTree()) {
        it?.let { uri ->
            val flags = FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            roomUtilVM.importCsvToRoom(uri)
        }
    }
    val csvExportLauncher = rememberLauncherForActivityResult(contract = OpenDocumentTree()) {
        it?.let { uri ->
            val flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            roomUtilVM.setupAppDirectoryAndExportToCsv(uri)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = { dbRestoreLauncher.launch(null) }) {
            Text(text = "DB Restore Sample Database")
        }
        Button(
            onClick = {
                if (roomUtilVM.appDirectoryUri == null) {
                    dbBackupLauncher.launch(null)
                } else {
                    roomUtilVM.backupDatabase()
                }
            }
        ) {
            Text(text = "Db Backup Sample Database")
        }
        Button(onClick = { csvImportLauncher.launch(null) }) {
            Text(text = "CSV Import Sample Database")
        }
        Button(
            onClick = {
                if (roomUtilVM.appDirectoryUri == null) {
                    csvExportLauncher.launch(null)
                } else {
                    roomUtilVM.exportToCsv()
                }
            }
        ) {
            Text(text = "CSV Export Sample Database")
        }
        Button(onClick = { roomUtilVM.updateAppDirectoryUriToNull()}) {
            Text(text = "Clear selected app directory")
        }
    }
}