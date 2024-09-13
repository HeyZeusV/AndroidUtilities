package com.heyzeusv.androidutilities.room

interface CsvInfo {
    val csvFileName: String
    val csvHeader: List<String>
    val csvRow: List<Any?>
}