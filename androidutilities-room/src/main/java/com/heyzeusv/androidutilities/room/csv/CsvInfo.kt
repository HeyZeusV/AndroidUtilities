package com.heyzeusv.androidutilities.room.csv

interface CsvInfo {
    val csvFileName: String
    val csvFieldToTypeMap: Map<String, String>
}

interface CsvData {
    val csvRow: List<Any?>
}