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
    fun updateAppDirectoryUriToNull() { _appDirectoryUri = null }

    fun importCsvToRoom(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = csvConverter.importCsvToRoom(selectedDirectoryUri)

            Log.d("SampleApp", "Data: $data")
        }
    }

    fun setupAppDirectoryAndExportToCsv(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _appDirectoryUri = csvConverter.findOrCreateAppDirectory(selectedDirectoryUri)
            _appDirectoryUri?.let {
                val roomData = RoomData()
                val directoryUri = _appDirectoryUri ?: return@launch

                csvConverter.exportRoomToCsv(directoryUri, roomData)
            }
        }
    }

    fun exportToCsv() {
        viewModelScope.launch(Dispatchers.IO) {
            val roomData = RoomData()
            val directoryUri = _appDirectoryUri ?: return@launch

            csvConverter.exportRoomToCsv(directoryUri, roomData)
        }
    }
}