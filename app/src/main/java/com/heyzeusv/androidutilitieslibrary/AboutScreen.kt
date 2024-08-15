package com.heyzeusv.androidutilitieslibrary

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.heyzeusv.androidutilities.compose.ui.about.AboutScreen
import com.heyzeusv.androidutilities.compose.ui.about.LibraryItem
import com.heyzeusv.androidutilitieslibrary.ui.theme.AndroidUtilitiesLibraryTheme
import com.heyzeusv.androidutilitieslibrary.util.fakeLibrary

@Preview
@Composable
fun AboutScreenPreview() {
    AndroidUtilitiesLibraryTheme {
        AboutScreen()
    }
}

@Preview
@Composable
fun LibraryItemPreview() {
    AndroidUtilitiesLibraryTheme {
        LibraryItem(fakeLibrary)
    }
}