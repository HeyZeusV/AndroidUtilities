package com.heyzeusv.androidutilitieslibrary.feature.roomutil

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.androidutilitieslibrary.database.CsvConverter
import com.heyzeusv.androidutilitieslibrary.database.Repository
import com.heyzeusv.androidutilitieslibrary.database.RoomBackupRestore
import com.heyzeusv.androidutilitieslibrary.database.RoomData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomUtilViewModel @Inject constructor(
    private val repository: Repository,
    private val roomBackupRestore: RoomBackupRestore,
    private val csvConverter: CsvConverter,
) : ViewModel() {

    private var _appDirectoryUri: Uri? = null
    val appDirectoryUri: Uri? get() = _appDirectoryUri
    fun updateAppDirectoryUriToNull() { _appDirectoryUri = null }

    fun restoreDatabase(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            roomBackupRestore.restore(selectedDirectoryUri)
            // TODO: Call checkpoint here
        }
    }

    fun setupAppDirectoryAndBackupDatabase(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _appDirectoryUri = roomBackupRestore.findOrCreateAppDirectory(selectedDirectoryUri)
            _appDirectoryUri?.let {
                val directoryUri = _appDirectoryUri ?: return@launch

                roomBackupRestore.backup(directoryUri)
                // TODO: call checkpoint here
            }
        }
    }

    fun backupDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val directoryUri = _appDirectoryUri ?: return@launch

            roomBackupRestore.restore(directoryUri)
            // TODO: call checkpoint here
        }
    }

    fun importCsvToRoom(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = csvConverter.importCsvToRoom(selectedDirectoryUri)

            // TODO: Call fts rebuild here
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