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
        NavHost(
            navController = navController,
            startDestination = "about"
        ) {
            composable("about") {
                AboutScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this,
                    libraryOnClick = { partyId, index ->
                        navController.navigate("details/$partyId/$index")
                    }
                )
            }
            composable(
                route = "details/{libraryParty}/{libraryIndex}",
                arguments = listOf(
                    navArgument("libraryParty") { type = NavType.StringType },
                    navArgument("libraryIndex") { type = NavType.IntType}
                )
            ) { backStackEntry ->
                val libraryParty = backStackEntry.arguments?.getString("libraryParty")!!
                val libraryIndex = backStackEntry.arguments?.getInt("libraryIndex")!!
                val library = libraries[LibraryPartyInfo from libraryParty]!![libraryIndex]
                val sharedKey = "library-$libraryParty-$libraryIndex"

                LibraryScreen(
                    modifier = Modifier
                        .clickable { navController.navigate("about") }
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(sharedKey),
                            animatedVisibilityScope = this
                        ),
                    library = library,
                )
            }
        }
    }
}