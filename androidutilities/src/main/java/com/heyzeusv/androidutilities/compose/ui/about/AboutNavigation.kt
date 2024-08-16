package com.heyzeusv.androidutilities.compose.ui.about

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AboutNavigation() {
    SharedTransitionScope {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "about"
        ) {
            composable("about") {
                AboutScreen(
                    animatedVisibilityScope = this,
                    libraryOnClick = { index -> navController.navigate("details/$index") }
                )
            }
        }
    }
}