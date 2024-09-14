package com.heyzeusv.androidutilitieslibrary

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
import com.heyzeusv.androidutilities.room.csv.CsvData
import com.heyzeusv.androidutilities.room.csv.CsvInfo
import com.heyzeusv.androidutilities.room.csv.findOrCreateParentDirectory
import com.heyzeusv.androidutilitieslibrary.database.Database
import com.heyzeusv.androidutilitieslibrary.database.csv.CsvConverter
import com.heyzeusv.androidutilitieslibrary.database.models.CategoryRoomUtil
import com.heyzeusv.androidutilitieslibrary.database.models.DefaultItemRoomUtil
import com.heyzeusv.androidutilitieslibrary.database.models.ItemListRoomUtil
import com.heyzeusv.androidutilitieslibrary.database.models.ItemRoomUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RoomCsvScreen(db: Database) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(contract = OpenDocumentTree()) {
        it?.let { uri ->
            val flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            exportToCSV(context, coroutineScope, uri, db)
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
        val parentDirectoryUri = findOrCreateParentDirectory(
            context = context,
            parentDirectoryName = "RoomCsvExample",
            selectedDirectoryUri = selectedDirectoryUri
        )!!.toString()
        val dataMap = getData(db)
        CsvConverter(context).exportRoomToCsv(
            parentDirectoryUri = Uri.parse(parentDirectoryUri),
            dataMap = dataMap,
        )
    }
}

private suspend fun getData(db: Database): Map<CsvInfo, List<CsvData>> {
    val categories = db.categoryDao().getAll().map { CategoryRoomUtil.toUtil(it) }
    val defaultItems = db.defaultItemDao().getAll().map { DefaultItemRoomUtil.toUtil(it) }
    val items = db.itemDao().getAll().map { ItemRoomUtil.toUtil(it) }
    val itemLists = db.itemListDao().getAll().map { ItemListRoomUtil.toUtil(it) }
    val dataMap = mapOf(
        CategoryRoomUtil to categories, DefaultItemRoomUtil to defaultItems,
        ItemRoomUtil to items, ItemListRoomUtil to itemLists,
    )
    return dataMap
}