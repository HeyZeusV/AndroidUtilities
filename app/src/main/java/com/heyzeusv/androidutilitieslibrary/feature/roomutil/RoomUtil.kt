package com.heyzeusv.androidutilitieslibrary.feature.roomutil

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { dbRestoreLauncher.launch(null) },
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "DB Restore")
            }
            Button(
                onClick = {
                    if (roomUtilVM.appDbDirectoryUri == null) {
                        dbBackupLauncher.launch(null)
                    } else {
                        roomUtilVM.backupDatabase()
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Db Backup")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { csvImportLauncher.launch(null) },
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "CSV Import")
            }
            Button(
                onClick = {
                    if (roomUtilVM.appCsvDirectoryUri == null) {
                        csvExportLauncher.launch(null)
                    } else {
                        roomUtilVM.exportToCsv()
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "CSV Export")
            }
        }
        Button(onClick = { roomUtilVM.updateAppDirectoryUriToNull()}) {
            Text(text = "Clear sample app directory")
        }
    }
}