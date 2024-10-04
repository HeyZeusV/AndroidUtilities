# Room Utilities

KSP Annotation processor for Android Room that generates files for backup/restore and export/import
of database.

## Common

Both methods, RoomBackupRestore and CsvConverter, inherit from RoomUtilBase.
```kotlin
class RoomUtilBase(
    // used to read/write files and restart the app after restore
    val context: Context,
    // name of directory that will be created to store backups/exports
    val appDirectoryName: String,
) {
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
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")
}
```

[1]: https://www.sqlite.org/pragma.html#pragma_wal_checkpoint
[2]: https://github.com/HeyZeusV/AndroidUtilities/blob/feature_room_utilities/app/src/main/java/com/heyzeusv/androidutilitieslibrary/database/dao/AllDao.kt
[3]: https://github.com/HeyZeusV/AndroidUtilities/blob/feature_room_utilities/app/src/main/java/com/heyzeusv/androidutilitieslibrary/database/Repository.kt
[4]: https://www.sqlite.org/autoinc.html
[5]: https://www.sqlite.org/fts3.html#*fts4rebuidcmd
[6]: https://github.com/jsoizo/kotlin-csv