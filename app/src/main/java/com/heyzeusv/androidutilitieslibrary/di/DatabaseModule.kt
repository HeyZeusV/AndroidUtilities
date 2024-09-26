package com.heyzeusv.androidutilitieslibrary.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heyzeusv.androidutilitieslibrary.database.Database
import com.heyzeusv.androidutilitieslibrary.database.dao.AllDao
import com.heyzeusv.androidutilitieslibrary.database.dao.CategoryDao
import com.heyzeusv.androidutilitieslibrary.database.dao.DefaultItemDao
import com.heyzeusv.androidutilitieslibrary.database.dao.ItemDao
import com.heyzeusv.androidutilitieslibrary.database.dao.ItemListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    fun provideAllDao(database: Database): AllDao = database.allDao()

    @Provides
    fun provideItemListDao(database: Database): ItemListDao = database.itemListDao()

    @Provides
    fun provideItemDao(database: Database): ItemDao = database.itemDao()

    @Provides
    fun provideDefaultItemDao(database: Database): DefaultItemDao = database.defaultItemDao()

    @Provides
    fun provideCategoryDao(database: Database): CategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Room.databaseBuilder(
            context,
            Database::class.java,
            "Database.db"
        )
//            .createFromAsset("InitDatabase.db")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("INSERT INTO DefaultItemFts(DefaultItemFts) VALUES ('rebuild')")
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }
}