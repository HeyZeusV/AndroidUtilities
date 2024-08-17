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
                    libraryOnClick = { partyId, libraryId ->
                        navController.navigate("details/$partyId/$libraryId")
                    }
                )
            }
            composable(
                route = "details/{partyId}/{libraryId}",
                arguments = listOf(
                    navArgument("partyId") { type = NavType.StringType },
                    navArgument("libraryId") { type = NavType.StringType}
                )
            ) { backStackEntry ->
                val partyId = backStackEntry.arguments?.getString("partyId")!!
                val libraryId = backStackEntry.arguments?.getString("libraryId")!!
                val libs = libraries[LibraryPartyInfo from partyId]!!
                val library = libs.find { it.uniqueId == libraryId }!!
                val sharedKey = "library-$libraryId"

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