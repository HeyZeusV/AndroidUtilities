package com.heyzeusv.androidutilities.compose.ui.about

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AboutNavigation() {
    SharedTransitionLayout {
        val navController = rememberNavController()
        val libraries by produceLibraryState(true)

        // TODO: Try out both sharedElement and sharedBounds
        // TODO: Change libraries to only return one list
        NavHost(
            navController = navController,
            startDestination = "about"
        ) {
            composable("about") {
                AboutScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this,
                    libraryOnClick = { index -> navController.navigate("details/$index") }
                )
            }
            composable(
                route = "details/{library}",
                arguments = listOf(navArgument("library") { type = NavType.IntType})
            ) { backStackEntry ->
                val libraryId = backStackEntry.arguments?.getInt("library") ?: 0
                val library = libraries.second[libraryId]

                LibraryScreen(
                    modifier = Modifier
                        .clickable { navController.navigate("about") }
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "library-$libraryId"),
                            animatedVisibilityScope = this
                        ),
                    library = library,
                )
            }
        }
    }
}