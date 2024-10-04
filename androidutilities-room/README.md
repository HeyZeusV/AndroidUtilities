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

[1]: https://www.sqlite.org/pragma.html#pragma_wal_checkpoint
[2]: https://github.com/HeyZeusV/AndroidUtilities/blob/feature_room_utilities/app/src/main/java/com/heyzeusv/androidutilitieslibrary/database/dao/AllDao.kt
[3]: https://github.com/HeyZeusV/AndroidUtilities/blob/feature_room_utilities/app/src/main/java/com/heyzeusv/androidutilitieslibrary/database/Repository.kt

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
) {
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