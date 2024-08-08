package com.heyzeusv.androidutilitieslibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.heyzeusv.androidutilitieslibrary.ui.theme.AndroidUtilitiesLibraryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidUtilitiesLibraryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ComposableResources(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ComposableResources(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
    }
}

@Preview
@Composable
fun ComposableResourcesPreview() {
    AndroidUtilitiesLibraryTheme {
        ComposableResources()
    }
}