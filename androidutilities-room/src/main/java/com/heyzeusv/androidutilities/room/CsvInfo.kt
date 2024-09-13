package com.heyzeusv.androidutilities.room

interface CsvInfo {
    val csvFileName: String
    val csvHeader: List<String>
}

interface CsvData {
    val csvRow: List<Any?>
}