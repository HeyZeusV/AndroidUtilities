package com.heyzeusv.androidutilitieslibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.heyzeusv.androidutilities.compose.annotatedstring.HyperlinkText
import kotlinx.collections.immutable.persistentMapOf

@Composable
fun AnnotatedStringScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HyperlinkText(
            text = "Testing hyper link composable",
            textStyle = MaterialTheme.typography.headlineLarge,
            linkTextToHyperlinks = persistentMapOf(
                "Testing" to "github.com",
                "composable" to "https://medium.com",
            ),
        )
        HyperlinkText(
            text = "Testing hyper link composable",
            textStyle = MaterialTheme.typography.bodySmall,
            linkStyle = MaterialTheme.typography.displayLarge,
            linkTextToHyperlinks = persistentMapOf(
                "Testing" to "github.com",
                "composable" to "https://medium.com",
            ),
            linkTextColor = Color.Red,
            linkTextFontWeight = FontWeight.Bold,
            linkTextDecoration = TextDecoration.LineThrough,
        )
        HyperlinkText(
            textId = R.string.hyperlink_example,
            textStyle = MaterialTheme.typography.headlineLarge,
            linkTextToHyperlinks = persistentMapOf(
                "LINK1" to "github.com",
                "LINK2" to "https://medium.com",
            ),
        )
    }
}