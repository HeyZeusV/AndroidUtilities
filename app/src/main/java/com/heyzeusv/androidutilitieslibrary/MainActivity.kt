package com.heyzeusv.androidutilitieslibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.heyzeusv.androidutilities.compose.ui.AboutScreen
import com.heyzeusv.androidutilities.compose.util.avRes
import com.heyzeusv.androidutilities.compose.util.bRes
import com.heyzeusv.androidutilities.compose.util.cRes
import com.heyzeusv.androidutilities.compose.util.dRes
import com.heyzeusv.androidutilities.compose.util.iRes
import com.heyzeusv.androidutilities.compose.util.iaRes
import com.heyzeusv.androidutilities.compose.util.pRes
import com.heyzeusv.androidutilities.compose.util.psRes
import com.heyzeusv.androidutilities.compose.util.sRes
import com.heyzeusv.androidutilities.compose.util.saRes
import com.heyzeusv.androidutilitieslibrary.ui.theme.AndroidUtilitiesLibraryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidUtilitiesLibraryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AboutScreen()
                    ComposableResources(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun ComposableResources(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column {
            Text(
                text = "ComposableResources examples",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge,
            )
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = sRes(R.string.sRes_example),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = sRes(R.string.sRes_args_example, "with", "arguments"),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = saRes(R.array.saRes_example).joinToString(separator = " "),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = psRes(R.plurals.psRes_example, 1),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = psRes(R.plurals.psRes_example, 100, "with", "arguments"),
                    textAlign = TextAlign.Center,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "The icon below is loaded using pRes(), its size is loaded by dRes(), and its color is loaded by cRes()",
                        textAlign = TextAlign.Center,
                    )
                    Icon(
                        painter = pRes(R.drawable.pres_example),
                        contentDescription = null,
                        modifier = Modifier.size(dRes(R.dimen.dRes_example)),
                        tint = cRes(R.color.cRes_example)
                    )
                }
                val boolTrue = bRes(R.bool.bRes_true_example)
                val boolFalse = bRes(R.bool.bRes_false_example)
                Text(
                    text = "The following booleans were loaded using bRes(), $boolTrue and $boolFalse",
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "This number, ${iRes(R.integer.iRes_example)}, is loaded using iRes()",
                    textAlign = TextAlign.Center,
                )
                val numbers = iaRes(R.array.iaRes_example)
                Text(
                    text = "These numbers, ${numbers.joinToString()}, are loaded using iaRes()",
                    textAlign = TextAlign.Center,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "The icon below is loaded using avRes(), its size is loaded by dRes(), and can be clicked to restart animation",
                        textAlign = TextAlign.Center,
                    )
                    val image = avRes(R.drawable.avres_example)
                    var atEnd by remember { mutableStateOf(false) }
                    Image(
                        painter = rememberAnimatedVectorPainter(
                            animatedImageVector = image,
                            atEnd = atEnd
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(dRes(R.dimen.dRes_example))
                            .clickable { atEnd = !atEnd }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ComposableResourcesPreview() {
    AndroidUtilitiesLibraryTheme {
        ComposableResources()
    }
}