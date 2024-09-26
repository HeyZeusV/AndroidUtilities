package com.heyzeusv.androidutilitieslibrary.di

import android.content.Context
import com.heyzeusv.androidutilitieslibrary.database.CsvConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Provides
    @Singleton
    fun provideCsvConverter(@ApplicationContext context: Context): CsvConverter =
        CsvConverter(context, "RoomUtilSample")
}