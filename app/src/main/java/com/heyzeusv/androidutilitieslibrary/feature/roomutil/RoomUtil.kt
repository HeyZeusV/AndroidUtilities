package com.heyzeusv.androidutilitieslibrary.feature.roomutil

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heyzeusv.androidutilities.compose.pagerindicator.HorizontalPagerIndicator
import com.heyzeusv.androidutilitieslibrary.database.RoomUtilStatus
import com.heyzeusv.androidutilitieslibrary.database.models.Category
import com.heyzeusv.androidutilitieslibrary.database.models.Item

@Composable
fun RoomUtilScreen(
    roomUtilVM: RoomUtilViewModel,
) {
    val context = LocalContext.current

    val csvStatus by roomUtilVM.csvStatus.collectAsStateWithLifecycle()
    val categories by roomUtilVM.categories.collectAsStateWithLifecycle()
    val items by roomUtilVM.items.collectAsStateWithLifecycle()

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

    RoomUtilScreen(
        csvStatus = csvStatus,
        categories = categories,
        items = items,
        dbRestoreOnClick = { dbRestoreLauncher.launch(null) },
        dbBackupOnClick = {
            if (roomUtilVM.appDbDirectoryUri == null) {
                dbBackupLauncher.launch(null)
            } else {
                roomUtilVM.backupDatabase()
            }
        },
        csvImportOnClick = { csvImportLauncher.launch(null) },
        csvExportOnClick = {
            if (roomUtilVM.appCsvDirectoryUri == null) {
                csvExportLauncher.launch(null)
            } else {
                roomUtilVM.exportToCsv()
            }
        },
        clearUriOnClick = { roomUtilVM.updateAppDirectoryUriToNull() },
        deleteAllOnClick = { roomUtilVM.deleteAll() },
        insertCategoriesOnClick = { roomUtilVM.insert1000RandomCategories()},
        insertItemsOnClick = { roomUtilVM.insert1000RandomItems() },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RoomUtilScreen(
    csvStatus: RoomUtilStatus,
    categories: List<Category>,
    items: List<Item>,
    dbRestoreOnClick: () -> Unit,
    dbBackupOnClick: () -> Unit,
    csvImportOnClick: () -> Unit,
    csvExportOnClick: () -> Unit,
    clearUriOnClick: () -> Unit,
    deleteAllOnClick: () -> Unit,
    insertCategoriesOnClick: () -> Unit,
    insertItemsOnClick: () -> Unit,
) {
    val pagerState = rememberPagerState { 2 }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TwoButtonRow(
                leftText = "DB Restore",
                leftOnClick = dbRestoreOnClick,
                rightText = "Db Backup",
                rightOnClick = dbBackupOnClick,
            )
            TwoButtonRow(
                leftText = "CSV Import",
                leftOnClick = csvImportOnClick,
                rightText = "CSV Export",
                rightOnClick = csvExportOnClick,
            )
            TwoButtonRow(
                leftText = "Clear App Directory",
                leftOnClick = clearUriOnClick,
                rightText = "Clear Database",
                rightOnClick = deleteAllOnClick,
            )
            TwoButtonRow(
                leftText = "Insert 1k Categories",
                leftOnClick = insertCategoriesOnClick,
                rightText = "Insert 1k Items",
                rightOnClick = insertItemsOnClick,
                rightEnabled = categories.isNotEmpty()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                Text(
                    text = "# of Categories: ${categories.size}",
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${items.size} : # of Items",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                )
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                when (page) {
                    0 -> CategoryPage(categories = categories)
                    1 -> ItemPage(items = items)
                }
            }
            HorizontalPagerIndicator(
                pagerState = pagerState,
                pageCount = 2,
            )
        }
        Status(
            snackbarHostState = snackbarHostState,
            csvStatus = csvStatus,
        )
    }
}

@Composable
fun Status(
    snackbarHostState: SnackbarHostState,
    csvStatus: RoomUtilStatus,
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = csvStatus) {
        when (csvStatus) {
            is RoomUtilStatus.Error -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(csvStatus.messageId, csvStatus.name),
                    duration = SnackbarDuration.Short,
                )
            }
            is RoomUtilStatus.Success -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(csvStatus.messageId),
                    duration = SnackbarDuration.Short,
                )
            }
            else -> {}
        }
    }

    if (csvStatus is RoomUtilStatus.Progress) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6000000))
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(250.dp),
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round,
                )
                Text(
                    text = stringResource(csvStatus.messageId, csvStatus.name),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
        }
    }
}

@Composable
fun TwoButtonRow(
    leftText: String,
    leftOnClick: () -> Unit,
    leftEnabled: Boolean = true,
    rightText: String,
    rightOnClick: () -> Unit,
    rightEnabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = leftOnClick,
            modifier = Modifier.weight(1f),
            enabled = leftEnabled,
        ) {
            Text(text = leftText)
        }
        Button(
            onClick = rightOnClick,
            modifier = Modifier.weight(1f),
            enabled = rightEnabled,
        ) {
            Text(text = rightText)
        }
    }
}

@Composable
fun CategoryPage(categories: List<Category>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(
            items = categories,
            key = { it.id }
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    Text(
                        text = "Id: ${it.id}",
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "Name: ${it.name}",
                        modifier = Modifier.weight(1f),
                    )
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ItemPage(items: List<Item>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(
            items = items,
            key = { it.itemId }
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    Text(
                        text = "Id: ${it.itemId}",
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "Name: ${it.name}",
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    Text(
                        text = "Category: ${it.category}",
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "Unit: ${it.unit}",
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(text = "Quantity: ${it.quantity}")
                Text(text = "Outer Embed Same Name: ${it.outerEmbed.sameName}")
                Text(text = "Inner Embed Same Name: ${it.outerEmbed.embed.sameName}")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    Text(
                        text = "Short: ${it.outerEmbed.embed.nullableShort}",
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "ByteArray: ${String(it.outerEmbed.embed.nullableByteArray!!)}",
                        modifier = Modifier.weight(1f),
                    )
                }
                HorizontalDivider()
            }
        }
    }
}