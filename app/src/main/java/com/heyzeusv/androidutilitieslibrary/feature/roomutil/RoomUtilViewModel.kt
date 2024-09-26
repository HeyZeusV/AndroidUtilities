package com.heyzeusv.androidutilitieslibrary.feature.roomutil

import android.net.Uri
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

    private var _appDbDirectoryUri: Uri? = null
    val appDbDirectoryUri: Uri? get() = _appDbDirectoryUri

    private var _appCsvDirectoryUri: Uri? = null
    val appCsvDirectoryUri: Uri? get() = _appCsvDirectoryUri

    fun updateAppDirectoryUriToNull() {
        _appDbDirectoryUri = null
        _appCsvDirectoryUri = null

    }

    fun restoreDatabase(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.callCheckpoint()
            roomBackupRestore.restore(selectedDirectoryUri)
        }
    }

    fun setupAppDirectoryAndBackupDatabase(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _appDbDirectoryUri = roomBackupRestore.findOrCreateAppDirectory(selectedDirectoryUri)
            _appDbDirectoryUri?.let {
                val directoryUri = _appDbDirectoryUri ?: return@launch

                repository.callCheckpoint()
                roomBackupRestore.backup(directoryUri)
            }
        }
    }

    fun backupDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val directoryUri = _appDbDirectoryUri ?: return@launch

            repository.callCheckpoint()
            roomBackupRestore.backup(directoryUri)
        }
    }

    fun importCsvToRoom(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = csvConverter.importCsvToRoom(selectedDirectoryUri)

            data?.let {
                repository.transactionProvider.runAsTransaction {
                    repository.run {
                        deleteAll()
                        insertRoomData(it)
                        rebuildDefaultItemFts()
                    }
                }
            }
        }
    }

    fun setupAppDirectoryAndExportToCsv(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _appCsvDirectoryUri = csvConverter.findOrCreateAppDirectory(selectedDirectoryUri)
            _appCsvDirectoryUri?.let {
                val roomData = RoomData()
                val directoryUri = _appCsvDirectoryUri ?: return@launch

                csvConverter.exportRoomToCsv(directoryUri, roomData)
            }
        }
    }

    fun exportToCsv() {
        viewModelScope.launch(Dispatchers.IO) {
            val roomData = repository.getAllRoomData()
            val directoryUri = _appCsvDirectoryUri ?: return@launch

            csvConverter.exportRoomToCsv(directoryUri, roomData)
        }
    }
}