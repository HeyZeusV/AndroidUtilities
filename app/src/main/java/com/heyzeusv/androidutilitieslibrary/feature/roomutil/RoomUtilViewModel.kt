package com.heyzeusv.androidutilitieslibrary.feature.roomutil

import androidx.lifecycle.ViewModel
import com.heyzeusv.androidutilitieslibrary.database.Database
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RoomUtilViewModel @Inject constructor(
    private val database: Database
) : ViewModel()