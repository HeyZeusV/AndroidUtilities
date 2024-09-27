package com.heyzeusv.androidutilitieslibrary.di

import android.content.Context
import com.heyzeusv.androidutilitieslibrary.database.CsvConverter
import com.heyzeusv.androidutilitieslibrary.database.RoomBackupRestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Provides
    @Singleton
    fun provideCsvConverter(@ApplicationContext context: Context): CsvConverter =
        CsvConverter(context, "RoomUtilCsvSample")

    @Provides
    @Singleton
    fun provideRoomBackupRestore(@ApplicationContext context: Context): RoomBackupRestore =
        RoomBackupRestore(
            context = context,
            dbFileName = "Database.db",
            appDirectoryName = "RoomUtilDbSample"
        )

    @Provides
    @IODispatcher
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IODispatcher