package com.heyzeusv.androidutilitieslibrary.feature

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heyzeusv.androidutilities.compose.about.AboutScreen
import com.heyzeusv.androidutilities.compose.util.pRes
import com.heyzeusv.androidutilities.compose.util.sRes
import com.heyzeusv.androidutilitieslibrary.R
import com.heyzeusv.androidutilitieslibrary.util.hyperlinkCustomStringResourceEntry
import com.heyzeusv.androidutilitieslibrary.util.hyperlinkStringResource
import com.heyzeusv.androidutilitieslibrary.util.lorenEntry
import com.heyzeusv.androidutilitieslibrary.util.lorenHyperlinkCustomStringEntry
import com.heyzeusv.androidutilitieslibrary.util.lorenHyperlinkEntry
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AppAboutScreen(
    backOnClick: () -> Unit,
) {
    AboutScreen(
        backButton = {
            IconButton(
                onClick = { backOnClick() },
                modifier = Modifier.padding(all = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        icon = {
            Icon(
                painter = pRes(R.drawable.pres_example),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(50.dp)
            )
        },
        title = sRes(R.string.app_name),
        version = "v1.0.0",
        infoList = persistentListOf(
            lorenHyperlinkEntry,
            lorenHyperlinkCustomStringEntry(),
            hyperlinkStringResource,
            hyperlinkCustomStringResourceEntry(),
            lorenEntry,
            lorenEntry,
            lorenEntry
        ),
    )
}

@Composable
fun AppAboutScreenNoIcon(
    backOnClick: () -> Unit,
) {
    AboutScreen(
        backButton = {
            IconButton(
                onClick = { backOnClick() },
                modifier = Modifier.padding(all = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        title = sRes(R.string.app_name),
        version = "v1.0.0",
        infoList = persistentListOf(
            lorenHyperlinkEntry,
            lorenHyperlinkCustomStringEntry(),
            hyperlinkStringResource,
            hyperlinkCustomStringResourceEntry(),
            lorenEntry,
            lorenEntry,
            lorenEntry
        ),
    )
}

@Composable
fun AppAboutScreenNoBackOrIcon() {
    AboutScreen(
        title = sRes(R.string.app_name),
        version = "v1.0.0",
        infoList = persistentListOf(
            lorenHyperlinkEntry,
            lorenHyperlinkCustomStringEntry(),
            hyperlinkStringResource,
            hyperlinkCustomStringResourceEntry(),
            lorenEntry,
            lorenEntry,
            lorenEntry
        ),
    )
}