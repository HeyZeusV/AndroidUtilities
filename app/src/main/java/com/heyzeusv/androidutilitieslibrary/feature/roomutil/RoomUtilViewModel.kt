package com.heyzeusv.androidutilitieslibrary.feature.roomutil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.androidutilitieslibrary.database.CsvConverter
import com.heyzeusv.androidutilitieslibrary.database.Repository
import com.heyzeusv.androidutilitieslibrary.database.RoomBackupRestore
import com.heyzeusv.androidutilitieslibrary.database.models.Category
import com.heyzeusv.androidutilitieslibrary.database.models.Item
import com.heyzeusv.androidutilitieslibrary.database.models.SampleInnerEmbed
import com.heyzeusv.androidutilitieslibrary.database.models.SampleOuterEmbed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class RoomUtilViewModel @Inject constructor(
    private val repository: Repository,
    private val roomBackupRestore: RoomBackupRestore,
    private val csvConverter: CsvConverter,
) : ViewModel() {

    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    val categories = repository.getAllCategoriesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList(),
        )

    val items = repository.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList(),
        )

    private var _appDbDirectoryUri: Uri? = null
    val appDbDirectoryUri: Uri? get() = _appDbDirectoryUri

    private var _appCsvDirectoryUri: Uri? = null
    val appCsvDirectoryUri: Uri? get() = _appCsvDirectoryUri

    fun updateAppDirectoryUriToNull() {
        _appDbDirectoryUri = null
        _appCsvDirectoryUri = null
    }

    private var _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> get() = _isBusy.asStateFlow()
    private suspend fun isBusyRun(block: suspend () -> Unit) {
        run {
            _isBusy.value = true
            block()
            _isBusy.value = false
        }
    }

    /**
     *  Create string of random characters with given [size].
     */
    private fun randomString(size: Int = 8) = List(size) { charPool.random() }.joinToString("")

    /**
     *  Add 1000 Categories to database with random name.
     */
    fun insert1000RandomCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            isBusyRun {
                val categoryList = mutableListOf<Category>()
                repeat(1000) {
                    val category = Category(name = randomString())
                    categoryList.add(category)
                }
                repository.insertCategories(*categoryList.toTypedArray())
            }
        }
    }

    /**
     *  Add 1000 Items to database with mostly random values.
     */
    fun insert1000RandomItems() {
        viewModelScope.launch(Dispatchers.IO) {
            isBusyRun {
                val itemList = mutableListOf<Item>()
                repeat(1000) {
                    val item = Item(
                        name = randomString(),
                        category = categories.value.random().name,
                        quantity = Random.nextDouble(),
                        unit = randomString(4),
                        memo = randomString(32),
                        outerEmbed = SampleOuterEmbed(
                            sameName = randomString(),
                            someField = Random.nextInt(),
                            uselessField = Random.nextLong(),
                            embed = SampleInnerEmbed(
                                sameName = randomString(),
                                nullableBoolean = Random.nextBoolean(),
                                nullableShort = null,
                                nullableInt = Random.nextInt(),
                                nullableLong = Random.nextLong(),
                                nullableByte = null,
                                nullableString = randomString(),
                                nullableChar = null,
                                nullableDouble = Random.nextDouble(),
                                nullableFloat = Random.nextFloat(),
                                nullableByteArray = Random.nextBytes(4)
                            )
                        )
                    )
                    itemList.add(item)
                }
                repository.upsertItems(*itemList.toTypedArray())
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            isBusyRun {
                repository.deleteAll()
            }
        }
    }

    fun restoreDatabase(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            isBusyRun {
                repository.callCheckpoint()
                roomBackupRestore.restore(selectedDirectoryUri)
            }
        }
    }

    fun setupAppDirectoryAndBackupDatabase(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            isBusyRun {
                _appDbDirectoryUri =
                    roomBackupRestore.findOrCreateAppDirectory(selectedDirectoryUri)
                _appDbDirectoryUri?.let {
                    val directoryUri = _appDbDirectoryUri

                    directoryUri?.let {
                        repository.callCheckpoint()
                        roomBackupRestore.backup(directoryUri)
                    }
                }
            }
        }
    }

    fun backupDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            isBusyRun {
                val directoryUri = _appDbDirectoryUri

                directoryUri?.let {
                    repository.callCheckpoint()
                    roomBackupRestore.backup(directoryUri)
                }
            }
        }
    }

    fun importCsvToRoom(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            isBusyRun {
                val data = csvConverter.importCsvToRoom(selectedDirectoryUri)

                data?.let {
                    repository.transactionProvider.runAsTransaction {
                        repository.run {
                            deleteAll()
                            insertRoomData(it)
                            rebuildItemFts()
                        }
                    }
                }
            }
        }
    }

    fun setupAppDirectoryAndExportToCsv(selectedDirectoryUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            isBusyRun {
                _appCsvDirectoryUri = csvConverter.findOrCreateAppDirectory(selectedDirectoryUri)
                _appCsvDirectoryUri?.let {
                    val roomData = repository.getAllRoomData()
                    val directoryUri = _appCsvDirectoryUri

                    directoryUri?.let {
                        csvConverter.exportRoomToCsv(directoryUri, roomData)
                    }
                }
            }
        }
    }

    fun exportToCsv() {
        viewModelScope.launch(Dispatchers.IO) {
            isBusyRun {
                val roomData = repository.getAllRoomData()
                val directoryUri = _appCsvDirectoryUri

                directoryUri?.let {
                    csvConverter.exportRoomToCsv(directoryUri, roomData)
                }
            }
        }
    }
}