package com.heyzeusv.androidutilities.room.util

import com.squareup.kotlinpoet.ClassName

internal object Constants {
    const val EXTENSION_KT = "kt"

    const val FALSE = "false"
    const val TRUE = "true"

    const val OPTION_NAMESPACE = "roomUtilNamespace"
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
    const val STATUS_STANDBY = "Standby"
    const val STATUS_PROGRESS = "Progress"
    const val STATUS_ERROR = "Error"
    const val STATUS_SUCCESS = "Success"

    /**
     *  Parameters and/or Properties
     */
    const val APP_DIRECTORY_NAME = "appDirectoryName"
    const val CONTEXT = "context"
    const val SELECTED_DIRECTORY_URI = "selectedDirectoryUri"
    const val MESSAGE_ID = "messageId"
    const val NAME = "name"

    val contextClassName = ClassName("android.content", "Context")
    val documentFileClassName = ClassName("androidx.documentfile.provider", "DocumentFile")
    val injectClassName = ClassName("javax.inject", "Inject")
    val uriClassName = ClassName("android.net", "Uri")
    val stringResClassName = ClassName("androidx.annotation", "StringRes")
    val mutableStateFlowClassName = ClassName("kotlinx.coroutines.flow", "MutableStateFlow")
    val stateFlowClassName = ClassName("kotlinx.coroutines.flow", "StateFlow")
    val asStateFlowClassName = ClassName("kotlinx.coroutines.flow", "asStateFlow")
}