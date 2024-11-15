package com.heyzeusv.androidutilitieslibrary.feature.roomutil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyzeusv.androidutilitieslibrary.R
import com.heyzeusv.androidutilitieslibrary.database.CsvConverter
import com.heyzeusv.androidutilitieslibrary.database.Repository
import com.heyzeusv.androidutilitieslibrary.database.RoomBackupRestore
import com.heyzeusv.androidutilitieslibrary.database.RoomUtilStatus
import com.heyzeusv.androidutilitieslibrary.database.RoomUtilStatus.Progress
import com.heyzeusv.androidutilitieslibrary.database.RoomUtilStatus.Success
import com.heyzeusv.androidutilitieslibrary.database.models.Category
import com.heyzeusv.androidutilitieslibrary.database.models.Item
import com.heyzeusv.androidutilitieslibrary.database.models.SampleInnerEmbed
import com.heyzeusv.androidutilitieslibrary.database.models.SampleOuterEmbed
import com.heyzeusv.androidutilitieslibrary.di.IODispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class RoomUtilViewModel @Inject constructor(
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
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

    val csvStatus: StateFlow<RoomUtilStatus> get() = csvConverter.status

    /**
     *  Create string of random characters with given [size].
     */
    private fun randomString(size: Int = 8) = List(size) { charPool.random() }.joinToString("")

    /**
     *  Add 1000 Categories to database with random name.
     */
    fun insert1000RandomCategories() {
        viewModelScope.launch(ioDispatcher) {
            csvConverter.updateStatus(Progress(R.string.categories_add))
            val categoryList = mutableListOf<Category>()
            repeat(1000) {
                val category = Category(name = randomString())
                categoryList.add(category)
            }
            repository.insertCategories(*categoryList.toTypedArray())
            csvConverter.updateStatus(Success(R.string.categories_success))
        }
    }

    /**
     *  Add 1000 Items to database with mostly random values.
     */
    fun insert1000RandomItems() {
        viewModelScope.launch(ioDispatcher) {
            csvConverter.updateStatus(Progress(R.string.items_add))
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
                repository.upsertItems(*itemList.toTypedArray())
                csvConverter.updateStatus(Success(R.string.items_success))
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch(ioDispatcher) {
            csvConverter.updateStatus(Success(R.string.delete))
            repository.deleteAll()
            csvConverter.updateStatus(Success(R.string.delete_success))
        }
    }

    fun restoreDatabase(selectedDirectoryUri: Uri) {
        viewModelScope.launch(ioDispatcher) {
            repository.callCheckpoint()
            roomBackupRestore.restore(selectedDirectoryUri)
        }
    }

    fun setupAppDirectoryAndBackupDatabase(selectedDirectoryUri: Uri) {
        viewModelScope.launch(ioDispatcher) {
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

    fun backupDatabase() {
        viewModelScope.launch(ioDispatcher) {
            val directoryUri = _appDbDirectoryUri

            directoryUri?.let {
                repository.callCheckpoint()
                roomBackupRestore.backup(directoryUri)
            }
        }
    }

    fun importCsvToRoom(selectedDirectoryUri: Uri) {
        viewModelScope.launch(ioDispatcher) {
            val data = csvConverter.importCsvToRoom(selectedDirectoryUri)

            data?.let {
                csvConverter.updateStatus(Progress(R.string.import_progress_data))
                repository.transactionProvider.runAsTransaction {
                    repository.run {
                        deleteAll()
                        insertRoomData(it)
                        rebuildItemFts()
                    }
                }
                csvConverter.updateStatus(Success(R.string.import_success))
            }
        }
    }

    fun setupAppDirectoryAndExportToCsv(selectedDirectoryUri: Uri) {
        viewModelScope.launch(ioDispatcher) {
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

    fun exportToCsv() {
        viewModelScope.launch(ioDispatcher) {
            val roomData = repository.getAllRoomData()
            val directoryUri = _appCsvDirectoryUri

            directoryUri?.let {
                csvConverter.exportRoomToCsv(directoryUri, roomData)
            }
        }
    }
}