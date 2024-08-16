package com.heyzeusv.androidutilitieslibrary

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.heyzeusv.androidutilities.compose.ui.about.AboutScreen
import com.heyzeusv.androidutilitieslibrary.ui.theme.AndroidUtilitiesLibraryTheme
import com.heyzeusv.androidutilitieslibrary.util.fakeLongLibrary
import com.heyzeusv.androidutilitieslibrary.util.fakeShortLibrary
import com.heyzeusv.androidutilitieslibrary.util.lorem

@Preview
@Composable
fun AboutScreenPreview() {
    AndroidUtilitiesLibraryTheme {
        AboutScreen(
            info = listOf(lorem, lorem, lorem),
            libraries = Pair(
                listOf(fakeLongLibrary),
                listOf(fakeShortLibrary, fakeLongLibrary)
            )
        )
    }
}