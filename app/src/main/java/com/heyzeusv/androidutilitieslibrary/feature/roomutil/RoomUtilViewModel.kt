package com.heyzeusv.androidutilitieslibrary.feature.roomutil

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.androidutilitieslibrary.database.CsvConverter
import com.heyzeusv.androidutilitieslibrary.database.Database
import com.heyzeusv.androidutilitieslibrary.database.RoomData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomUtilViewModel @Inject constructor(
    private val database: Database,
    private val csvConverter: CsvConverter,
) : ViewModel() {

    private var _appDirectoryUri: Uri? = null
    val appDirectoryUri: Uri? get() = _appDirectoryUri

    fun importCsvToRoom(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = csvConverter.importCsvToRoom(selectedDirectoryUri)

            Log.d("SampleApp", "Data: $data")
        }
    }

    fun exportRoomToCsv() {
        val data = RoomData()
        val uri = _appDirectoryUri!!

        csvConverter.exportRoomToCsv(
            appExportDirectoryUri = uri,
            roomData = data,
        )
    }
}