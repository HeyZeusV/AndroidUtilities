package com.heyzeusv.androidutilitieslibrary.database

import androidx.room.withTransaction
import javax.inject.Inject

class TransactionProvider @Inject constructor(
    private val db: Database
) {
    suspend fun <R> runAsTransaction(block: suspend () -> R): R {
        return db.withTransaction(block)
    }
}