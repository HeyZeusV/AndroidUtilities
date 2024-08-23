package com.heyzeusv.androidutilitieslibrary

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.heyzeusv.androidutilities.compose.about.AboutScreen
import com.heyzeusv.androidutilities.compose.util.pRes
import com.heyzeusv.androidutilities.compose.util.sRes
import com.heyzeusv.androidutilitieslibrary.ui.theme.AndroidUtilitiesLibraryTheme
import com.heyzeusv.androidutilitieslibrary.util.lorem

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidUtilitiesLibraryTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = Screens.Overview,
                    ) {
                        composable<Screens.Overview> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Button(onClick = { navController.navigate(Screens.About) }) {
                                    Text(text = "About Screen")
                                }
                                Button(onClick = { navController.navigate(Screens.ComposableResources) }) {
                                    Text(text = "Composable Resources")
                                }
                            }
                        }
                        composable<Screens.About> {
                            AboutScreen(
                                icon = {
                                    Icon(
                                        painter = pRes(R.drawable.pres_example),
                                        contentDescription = null,
                                        modifier = Modifier.size(50.dp)
                                    )
                                },
                                title = sRes(R.string.app_name),
                                version = "v1.0.0",
                                info = listOf(lorem, lorem, lorem, lorem, lorem, lorem, lorem),
                            )
                        }
                        composable<Screens.ComposableResources> {
                            ComposableResources()
                        }
                    }
                }
            }
        }
    }
}