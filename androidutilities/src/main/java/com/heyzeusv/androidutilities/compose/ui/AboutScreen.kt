package com.heyzeusv.androidutilities.compose.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AboutScreen() {
    val context = LocalContext.current

    val libraries = produceState<Libs?>(null) {
        value = withContext(Dispatchers.IO) {
            Libs.Builder().withContext(context).build()
        }
    }
    Log.d("tag", "AboutScreen")
    Log.d("tag", "Libraries ${libraries.value?.libraries?.size}")
    Log.d("tag", "Libraries ${libraries.value?.licenses?.size}")
}