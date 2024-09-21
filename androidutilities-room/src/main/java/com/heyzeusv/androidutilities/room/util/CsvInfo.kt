package com.heyzeusv.androidutilities.room.util

/**
 *  Entity information implemented by *RoomUtil.companionObject, so it can be accessed at any time.
 */
interface CsvInfo {
    /**
     *  Name of file when converted to CSV.
     */
    val csvFileName: String

    /**
     *  Map of field names to their types as strings. Faster than having to use reflection to get
     *  these values.
     */
    val csvFieldToTypeMap: Map<String, String>
}

/**
 *  Entity information implemented by *RoomUtil.
 */
interface CsvData {
    /**
     *  All the values of properties in order of constructor.
     */
    val csvRow: List<Any?>
}