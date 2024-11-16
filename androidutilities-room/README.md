# Room Utilities

KSP Annotation processor for Android Room that generates files for backup/restore and export/import
of database.

## Common

### RoomUtilBase
Both classes, RoomBackupRestore and CsvConverter, inherit from RoomUtilBase.
```kotlin
class RoomUtilBase(
    // used to read/write files and restart the app after restore
    val context: Context,
    // name of directory that will be created to store backups/exports
    val appDirectoryName: String,
) {
    /**
     *  Flow that emits the current status of action (import/export/backup/restore). This can be
     *  observed on View layer to display progress updates, error messages, and success messages.
     */
    val status: StateFlow<RoomUtilStatus> = _status.asStateFlow()

    /**
     *  RoomUtilBase files automatically emit values to status, but this function allows custom
     *  updates to status.
     *  
     *  For example: csv import requries developer to import data to Room after CsvConverter has 
     *  read csv files and returned data as RoomData. So developer would want to pass
     *  ROomUtilStatus.Success to updateStatus to let user know that import finished.
     */
    fun updateStatus(newValue: RoomUtilStatus)
    
    /**
     *  Looks for directory with the name of [appDirectoryName] at given [selectedDirectoryUri] 
     *  and returns its [Uri]. If it is not found, then creates it at given [selectedDirectoryUri]
     *  and returns its [Uri]. Returns null if creating new directory fails.
     *  
     *  The returned [Uri] should be saved using SharedPreferences/Proto DataStore/etc since it
     *  will needed by backup/restore/export/import functions. Saving the [Uri] will skip the need
     *  to ask user on each app restart.
     */
    fun findOrCreateAppDirectory(selectedDirectoryUri: Uri): Uri?
}
```

status can be seen used on sample app [here][15].

### RoomUtilStatus
Sealed class used to provide updates to user on current status of RoomUtil action.
```kotlin
sealed class RoomUtilStatus {
    data object StandBy : RoomUtilStatus()
    
    data class Progress(
        @StringRes val messageID: Int, // string resource id of message to display
        val name: String = "", // argument for messageId 
    ) : RoomUtilStatus()
    
    data class Error(
        @StringRes val messageID: Int, // string resource id of message to display
        val name: String = "", // argument for messageId 
    ) : RoomUtilStatus()
    
    data class Success(
        @StringRes val messageID: Int, // string resource id of message to display
    ) : RoomUtilStatus()
}
```

### RoomUtilStatus String Resources
CsvConverter and RoomBackupRestore use the following string resources, which are required.
```xml
    <!-- CSV Import -->
    <string name="import_error_corrupt_file">Something went wrong while reading %1$s, please try again…</string>
    <string name="import_error_invalid_data">%1$s contains invalid data!</string>
    <string name="import_error_missing_directory">Selected directory is missing!</string>
    <string name="import_error_missing_file">%1$s is missing from selected directory!</string>
    <string name="import_progress_entity_success">Successfully read %1$s…</string>
    <string name="import_progress_started">Started importing data…</string>

    <!-- CSV Export -->
    <string name="export_error_create_directory_failed">Failed to create export directory, please try again…</string>
    <string name="export_error_create_file_failed">Something went wrong creating %1$s, please try again…</string>
    <string name="export_error_failed">Something went wrong exporting %1$s, please try again…</string>
    <string name="export_error_missing_directory">App directory is missing!</string>
    <string name="export_progress_entity_success">Successfully exported %1$s…</string>
    <string name="export_progress_started">Started exporting data…</string>
    <string name="export_success">Successfully exported data!</string>

    <!-- Room Backup -->
    <string name="backup_error_create_directory_failed">Failed to create backup directory, please try again…</string>
    <string name="backup_error_create_file_failed">Failed to create %1$s, please try again…</string>
    <string name="backup_error_failed">Failed to backup %1$s, please try again…</string>
    <string name="backup_error_missing_directory">Backup directory is missing!</string>
    <string name="backup_error_missing_file">Could not find %1$s…</string>
    <string name="backup_progress_file_success">%1$s backup successful…</string>
    <string name="backup_progress_started">Backup started…</string>
    <string name="backup_success">Backup successful!!</string>

    <!-- Room Restore -->
    <string name="restore_error_missing_db_file">Could not find database file…</string>
    <string name="restore_error_missing_directory">Restore directory is missing!</string>
    <string name="restore_progress_file_success">%1$s restore successful…</string>
    <string name="restore_progress_started">Restore started…</string>
    <string name="restore_success">Restore successful!! Restarting app…</string>
```

## Backup and Restore

This method backs up and restores the database file(s) (including wal and shm if they exist).

### Write-Ahead Logging (WAL)
WAL will be set to on automatically past Jelly Bean or could be set manually using
Room.databaseBuilder().setJournalMode() when creating the Database. If it is on, an additional
query is highly recommended to be called before calling RoomBackupRestore.backup(). 
Information on [wal_checkpoint][1]. [Sample Dao][2] and [Sample Repository][3]

```kotlin
@Dao
abstract class SomeDao {
    @RawQuery
    abstract suspend fun callCheckpoint(query: SupportSQLiteQuery): Int
}

class Repository(val someDao: SomeDao) {
    suspend fun callCheckpoint(): Int = withContext(Dispatchers.IO) {
        val query = SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE)")
        someDao.callCheckpoint(query)
    }
}
```

### RoomBackupRestore
```kotlin
class RoomBackupRestore(
    // used to read/write files and restart the app after restore
    val context: Context,
    // pass the exact name used in Room.databaseBuilder name parameter including the file extension!
    // used to find the database file(s)
    val dbFileName: String,
    // name of directory that will be created to store backups
    val appDirectoryName: String,
) : RoomUtilBase(context, appDirectoryName) {
    /**
     *  Backs up database file(s) to given [appBackupDirectory]. [appBackupDirectory] should
     *  be the [Uri] recieved from findOrCreateAppDirectory.
     *  
     *  Run checkpoint query BEFORE calling this
     */
    fun backup(appBackupDirectory: Uri)

    /**
     *  Restores the database file(s) fround at given [selectedDirectory]. After restoring file(s)
     *  the app requires a restart in order to properly show restored data. [restartApp] does come
     *  with a default value that does an immediate restart, but you could provide a custom one, if
     *  you require anything extra to run before restarting.
     */
    fun restore(selectedDirectory: Uri, restartApp: () -> Unit)
}
```

# Import and Export
This method converts database data to/from selected type (currently only CSV). Currently all data
is replaced on import.

### Primary Key Auto Generate
When using @PrimaryKey(autoGenerate = true), an additional table called "sqlite_sequence" is
created. When importing data, it is recommended to empty this table after deleting existing data
and before inserting new data. Additional information can be found [here][4]. 
[Sample Dao][2] and [Sample Repository][3].
```kotlin
@Dao
abstract class SomeDao {
    @Query("DELETE FROM sqlite_sequence")
    abstract suspend fun deleteAllPrimaryKeys()
}

class Repository(val someDao: SomeDao) {
    suspend fun deleteAll() {
        // delete entity data first
        allDao.deleteAllPrimaryKeys()
    }
}
```

### FTS4
Entities that have @Fts4 annotation are not included in import/export process. Instead it is 
recommended to rebuild them using rebuild command. Additional information can be found [here][5] at
linked section, as well as section 6.2.2 "External Content FTS4 Tables".
[Sample Dao][2] and [Sample Repository][3].
```kotlin
@Dao
abstract class SomeDao {
    @Query("INSERT INTO fts_table_name(fts_table_name) VALUES ('rebuild')")
    abstract suspend fun rebuildItemFts()
}

class Repository(val someDao: SomeDao) {
    suspend fun rebuildItemFts() = allDao.rebuildItemFts()

}
```
### RoomData
Data class used to pass/retrieve Room entity data to/from CsvConverter.
```kotlin
data class RoomData(
    val entityOneData: List<EntityOne>,
    val entityTwoData: List<EntityTwo>,
    // ...
    val entityNData: List<EntityN>,
)
```
### CsvConverter
Uses [kotlin-csv][6] library to read/write from/to CSV.
```kotlin
class CsvConverter(
    // used to read/write files and restart the app after restore
    val context: Context,
    // name of directory that will be created to store backups
    val appDirectoryName: String,
) : RoomUtilBase(context, appDirectoryName) {
    /**
     *  Imports CSV data found at given [selectedDirectoryUri] in the form of [RoomData]. Every
     *  entity table should have its own CSV file with the name matching @Entity.tableName or 
     *  class name if blank. Returns null if CSV file is missing, if data is the wrong type, or
     *  if there is an error opening a file. Existing Room data should only be deleted if this
     *  does not return null.
     */
    fun importCsvToRoom(selectedDirectoryUri: Uri): RoomData?
    
    /**
     *  Exports given [roomData] as CSV to [appExportDirectoryUri]. [appExportDirectoryUri] should
     *  be the [Uri] recieved from findOrCreateAppDirectory. If any entity fails to export,
     *  deletes previously successfully exported data and its directory of this export attempt.
     *  Creates a file for each entity, even if it is an empty table.
     */
    fun exportRoomToCsv(appExportDirectoryUri: Uri, roomData: RoomData)
}
```

## Gradle Options
In module build.gradle
```kotlin
ksp {
    // REQUIRED
    // used to import R class (Android resources)
    // use the same value as android.namespace in app build.gradle
    arg("roomUtilNamespace", "com.namespace.appname")
    // skips generating RoomBackupRestore
    // true by default
    arg("roomUtilDb", "false")
    // skips generating CsvConverter and its helper files
    // true by default
    arg("roomUtilCsv", "false")
    // adds @Inject to RoomBackupRestore and CsvConverter
    // false by default
    arg("roomUtilHilt", "true")
}
```

## Sample

### Code
[Sample Compose Screen][7] | [Sample ViewModel][8] | [Sample Database][9] | [Sample Hilt Modules][10]

### GIFs

[Backup][11]

[Restore][12]

[CSV Import][13]

[CSV Export][14]


## Installation

**Step 1.** Add Maven Central to root build.gradle
```kotlin
allprojects {
    repositories {
        // ...
        mavenCentral()
    }
}
```
or to settings.gradle (depending on your configuration)
```kotlin
dependencyResolutionManagement {
    // ...
    repositories {
        // ...
        mavenCentral()
    }
}
```

**Step 2.** Add dependencies to your module's build.gradle
```kotlin
dependencies {
    ksp("io.github.heyzeusv:roomutilities:1.0.0")
    // REQUIRED - read/write files
    implementation("androidx.documentfile:documentfile:1.0.1")
    // needed if you want CSV import/export
    implementation("com.jsoizo:kotlin-csv-jvm:1.10.0")
}
```

[1]: https://www.sqlite.org/pragma.html#pragma_wal_checkpoint
[2]: ../app/src/main/java/com/heyzeusv/androidutilitieslibrary/database/dao/AllDao.kt
[3]: ../app/src/main/java/com/heyzeusv/androidutilitieslibrary/database/Repository.kt
[4]: https://www.sqlite.org/autoinc.html
[5]: https://www.sqlite.org/fts3.html#*fts4rebuidcmd
[6]: https://github.com/jsoizo/kotlin-csv
[7]: ../app/src/main/java/com/heyzeusv/androidutilitieslibrary/feature/roomutil/RoomUtil.kt
[8]: ../app/src/main/java/com/heyzeusv/androidutilitieslibrary/feature/roomutil/RoomUtilViewModel.kt
[9]: ../app/src/main/java/com/heyzeusv/androidutilitieslibrary/database
[10]: ../app/src/main/java/com/heyzeusv/androidutilitieslibrary/di
[11]: ../images/RoomBackup.gif
[12]: ../images/RoomRestore.gif
[13]: ../images/CsvConverterImport.gif
[14]: ../images/CsvConverterExport.gif
[15]:../app/src/main/java/com/heyzeusv/androidutilitieslibrary/feature/roomutil/RoomUtil.kt
