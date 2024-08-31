package com.heyzeusv.androidutilitieslibrary.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.androidutilitieslibrary.database.models.Category
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CategoryDao : BaseDao<Category>("Category") {

    @Query("SELECT * " +
            "FROM Category " +
            "ORDER BY name ASC")
    abstract fun getAllCategories(): Flow<List<Category>>
}