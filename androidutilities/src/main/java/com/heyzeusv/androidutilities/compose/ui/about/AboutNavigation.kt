package com.heyzeusv.androidutilities.compose.ui.about

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.heyzeusv.androidutilities.compose.ui.library.LibraryColors
import com.heyzeusv.androidutilities.compose.ui.library.LibraryDefaults
import com.heyzeusv.androidutilities.compose.ui.library.LibraryDimensions
import com.heyzeusv.androidutilities.compose.ui.library.LibraryPadding
import com.heyzeusv.androidutilities.compose.ui.library.LibraryPartyInfo
import com.heyzeusv.androidutilities.compose.ui.library.LibraryScreen
import com.heyzeusv.androidutilities.compose.ui.library.LibraryTextStyles
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
    libraryColors: LibraryColors = LibraryDefaults.libraryScreenColors(),
    libraryPadding: LibraryPadding = LibraryDefaults.libraryPadding(),
    libraryDimensions: LibraryDimensions = LibraryDefaults.libraryScreenDimensions(),
    libraryTextStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
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
        libraryColors = libraryColors,
        libraryPadding = libraryPadding,
        libraryDimensions = libraryDimensions,
        libraryTextStyles = libraryTextStyles,
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
    libraryColors: LibraryColors = LibraryDefaults.libraryScreenColors(),
    libraryPadding: LibraryPadding = LibraryDefaults.libraryPadding(),
    libraryDimensions: LibraryDimensions = LibraryDefaults.libraryScreenDimensions(),
    libraryTextStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
) {
    SharedTransitionLayout {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "about"
        ) {
            composable(
                route = "about",
                enterTransition = { slideInVertically(initialOffsetY = { -it }) },
                exitTransition = { slideOutVertically(targetOffsetY = { -it }) },
            ) {
                AboutScreen(
                    animatedContentScope = this,
                    icon = icon,
                    title = title,
                    version = version,
                    info = info,
                    libraries = libraries,
                    libraryOnClick = { partyId, libraryId, pagerPage ->
                        navController.navigate("details/$partyId/$libraryId/$pagerPage")
                    },
                    colors = aboutColors,
                    padding = aboutPadding,
                    dimensions = aboutDimensions,
                    textStyles = aboutTextStyles,
                )
            }
            composable(
                route = "details/{partyId}/{libraryId}/{pagerPage}",
                arguments = listOf(
                    navArgument("partyId") { type = NavType.StringType },
                    navArgument("libraryId") { type = NavType.StringType},
                    navArgument("pagerPage") { type = NavType.IntType},
                )
            ) { backStackEntry ->
                val partyId = backStackEntry.arguments?.getString("partyId")!!
                val libraryId = backStackEntry.arguments?.getString("libraryId")!!
                val initialPagerPage = backStackEntry.arguments?.getInt("pagerPage")!!
                val libs = libraries[LibraryPartyInfo from partyId]!!
                val library = libs.find { it.uniqueId == libraryId }!!

                LibraryScreen(
                    animatedContentScope = this,
                    backOnClick = { navController.navigate("about") },
                    library = library,
                    initialPagerPage = initialPagerPage,
                    colors = libraryColors,
                    padding = libraryPadding,
                    dimensions = libraryDimensions,
                    textStyles = libraryTextStyles,
                )
            }
        }
    }
}