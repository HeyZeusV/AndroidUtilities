package com.heyzeusv.androidutilities.room.util

import com.squareup.kotlinpoet.ClassName

internal object Constants {
    const val EXTENSION_KT = "kt"

    const val FALSE = "false"
    const val TRUE = "true"

    const val OPTION_CSV = "roomUtilCsv"
    const val OPTION_DB = "roomUtilDb"
    const val OPTION_HILT = "roomUtilHilt"

    const val PACKAGE_DATABASE = "androidx.room.Database"
    const val PACKAGE_ENTITY = "androidx.room.Entity"
    const val PACKAGE_TYPE_CONVERTER = "androidx.room.TypeConverter"

    const val ANNOTATION_COLUMN_INFO = "ColumnInfo"
    const val ANNOTATION_EMBEDDED = "Embedded"
    const val ANNOTATION_ENTITY = "Entity"
    const val ANNOTATION_FTS4 = "Fts4"
    const val ANNOTATION_IGNORE = "Ignore"

    /**
     *  Class/File names
     */
    const val CSV_CONVERTER = "CsvConverter"
    const val CSV_DATA = "CsvData"
    const val CSV_INFO = "CsvInfo"
    const val ROOM_BACKUP_RESTORE = "RoomBackupRestore"
    const val ROOM_DATA = "RoomData"
    const val ROOM_UTIL_BASE = "RoomUtilBase"
    const val ROOM_UTIL_STATUS = "RoomUtilStatus"

    /**
     *  Parameters and/or Properties
     */
    const val APP_DIRECTORY_NAME = "appDirectoryName"
    const val CONTEXT = "context"
    const val SELECTED_DIRECTORY_URI = "selectedDirectoryUri"

    val contextClassName = ClassName("android.content", "Context")
    val documentFileClassName = ClassName("androidx.documentfile.provider", "DocumentFile")
    val injectClassName = ClassName("javax.inject", "Inject")
    val uriClassName = ClassName("android.net", "Uri")
}