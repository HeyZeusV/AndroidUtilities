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
import com.heyzeusv.androidutilities.compose.ui.library.LibraryPartyInfo
import com.heyzeusv.androidutilities.compose.ui.library.LibraryScreen
import com.heyzeusv.androidutilities.compose.ui.library.produceLibraryState
import com.mikepenz.aboutlibraries.entity.Library

@Composable
fun AboutNavigation(
    icon: @Composable () -> Unit = { },
    title: String,
    version: String,
    info: List<String>,
    separateByParty: Boolean = true,
    aboutColors: AboutColors = AboutDefaults.aboutColors(),
    aboutPadding: AboutPadding = AboutDefaults.aboutPadding(),
    aboutDimensions: AboutDimensions = AboutDefaults.aboutDimensions(),
    aboutTextStyles: AboutTextStyles = AboutDefaults.aboutTextStyles(),
) {
    val libraries by produceLibraryState(separateByParty = separateByParty)

    AboutNavigation(
        icon = icon,
        title = title,
        version = version,
        info = info,
        libraries = libraries,
        aboutColors = aboutColors,
        aboutPadding = aboutPadding,
        aboutDimensions = aboutDimensions,
        aboutTextStyles = aboutTextStyles,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AboutNavigation(
    icon: @Composable () -> Unit = { },
    title: String,
    version: String,
    info: List<String>,
    libraries: Map<LibraryPartyInfo, List<Library>>,
    aboutColors: AboutColors = AboutDefaults.aboutColors(),
    aboutPadding: AboutPadding = AboutDefaults.aboutPadding(),
    aboutDimensions: AboutDimensions = AboutDefaults.aboutDimensions(),
    aboutTextStyles: AboutTextStyles = AboutDefaults.aboutTextStyles(),
) {
    SharedTransitionLayout {
        val navController = rememberNavController()

        // TODO: Try out both sharedElement and sharedBounds
        NavHost(
            navController = navController,
            startDestination = "about"
        ) {
            composable("about") {
                AboutScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this,
                    icon = icon,
                    title = title,
                    version = version,
                    info = info,
                    libraries = libraries,
                    libraryOnClick = { partyId, libraryId ->
                        navController.navigate("details/$partyId/$libraryId")
                    },
                    colors = aboutColors,
                    padding = aboutPadding,
                    dimensions = aboutDimensions,
                    textStyles = aboutTextStyles,
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
                    colors = aboutColors.libraryColors,
                    padding = aboutPadding.libraryPadding,
                    dimensions = aboutDimensions.libraryDimensions,
                    textStyles = aboutTextStyles.libraryStyles,
                )
            }
        }
    }
}