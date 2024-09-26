package com.heyzeusv.androidutilitieslibrary.feature.roomutil

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.heyzeusv.androidutilitieslibrary.database.Database
import com.heyzeusv.androidutilitieslibrary.database.CsvConverter
import com.heyzeusv.androidutilitieslibrary.database.RoomData
import com.heyzeusv.androidutilitieslibrary.database.findOrCreateAppDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RoomUtilScreen(
    roomUtilVM: RoomUtilViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(contract = OpenDocumentTree()) {
        it?.let { uri ->
            val flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
//            exportToCSV(context, coroutineScope, uri, db)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = { exportLauncher.launch(null) }) {
            Text(text = "Export Sample Database")
        }
    }
}

private fun exportToCSV(
    context: Context,
    scope: CoroutineScope,
    selectedDirectoryUri: Uri,
    db: Database,
) {
    scope.launch(Dispatchers.IO) {
        val appExportDirectoryUri = findOrCreateAppDirectory(
            context = context,
            selectedDirectoryUri = selectedDirectoryUri,
            appDirectoryName = "RoomCsvExample",
        )!!
        val roomData = getData(db)
        CsvConverter(context).exportRoomToCsv(
            appExportDirectoryUri = appExportDirectoryUri,
            roomData = roomData,
        )
    }
}

private suspend fun getData(db: Database): RoomData{
    val categories = db.categoryDao().getAll()
    val defaultItems = db.defaultItemDao().getAll()
    val items = db.itemDao().getAll()
    val itemLists = db.itemListDao().getAll()
    return RoomData(
        categoryData = categories,
        defaultItemData = defaultItems,
        itemData = items,
        itemListData = itemLists
    )
}