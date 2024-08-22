package com.heyzeusv.androidutilities.compose.ui.about

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.heyzeusv.androidutilities.R
import com.heyzeusv.androidutilities.compose.ui.library.LibraryColors
import com.heyzeusv.androidutilities.compose.ui.library.LibraryDefaults
import com.heyzeusv.androidutilities.compose.ui.library.LibraryExtras
import com.heyzeusv.androidutilities.compose.ui.library.LibraryPadding
import com.heyzeusv.androidutilities.compose.ui.library.LibraryPartyInfo
import com.heyzeusv.androidutilities.compose.ui.library.LibraryScreen
import com.heyzeusv.androidutilities.compose.ui.library.LibraryTextStyles
import com.heyzeusv.androidutilities.compose.ui.library.produceLibraryState
import com.heyzeusv.androidutilities.compose.util.pRes
import com.mikepenz.aboutlibraries.entity.Library

@Composable
fun AboutNavigation(
    navController: NavHostController = rememberNavController(),
    icon: @Composable () -> Unit = { },
    title: String,
    version: String,
    info: List<String>,
    separateByParty: Boolean = true,
    aboutColors: AboutColors = AboutDefaults.aboutColors(),
    aboutPadding: AboutPadding = AboutDefaults.aboutPadding(),
    aboutExtras: AboutExtras = AboutDefaults.aboutExtras(),
    aboutTextStyles: AboutTextStyles = AboutDefaults.aboutTextStyles(),
    libraryColors: LibraryColors = LibraryDefaults.libraryColors(),
    libraryPadding: LibraryPadding =
        LibraryDefaults.libraryPadding(outerPadding = LibraryDefaults.ScreenOuterPV),
    libraryExtras: LibraryExtras =
        LibraryDefaults.libraryExtras(actionIcon = pRes(R.drawable.icon_collapse)),
    libraryTextStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
) {
    val libraries by produceLibraryState(separateByParty = separateByParty)

    AboutNavigation(
        navController = navController,
        icon = icon,
        title = title,
        version = version,
        info = info,
        libraries = libraries,
        aboutColors = aboutColors,
        aboutPadding = aboutPadding,
        aboutExtras = aboutExtras,
        aboutTextStyles = aboutTextStyles,
        libraryColors = libraryColors,
        libraryPadding = libraryPadding,
        libraryExtras = libraryExtras,
        libraryTextStyles = libraryTextStyles,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AboutNavigation(
    navController: NavHostController = rememberNavController(),
    icon: @Composable () -> Unit = { },
    title: String,
    version: String,
    info: List<String>,
    libraries: Map<LibraryPartyInfo, List<Library>>,
    aboutColors: AboutColors = AboutDefaults.aboutColors(),
    aboutPadding: AboutPadding = AboutDefaults.aboutPadding(),
    aboutExtras: AboutExtras = AboutDefaults.aboutExtras(),
    aboutTextStyles: AboutTextStyles = AboutDefaults.aboutTextStyles(),
    libraryColors: LibraryColors = LibraryDefaults.libraryColors(),
    libraryPadding: LibraryPadding =
        LibraryDefaults.libraryPadding(outerPadding = LibraryDefaults.ScreenOuterPV),
    libraryExtras: LibraryExtras =
        LibraryDefaults.libraryExtras(actionIcon = pRes(R.drawable.icon_collapse)),
    libraryTextStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = "about"
        ) {
            composable<AboutScreens.Overview> {
                AboutScreen(
                    animatedContentScope = this,
                    icon = icon,
                    title = title,
                    version = version,
                    info = info,
                    libraries = libraries,
                    libraryOnClick = { partyId, libraryId ->
                        navController.navigate(AboutScreens.LibraryDetails(partyId, libraryId))
                    },
                    colors = aboutColors,
                    padding = aboutPadding,
                    extras = aboutExtras,
                    textStyles = aboutTextStyles,
                )
            }
            composable<AboutScreens.LibraryDetails> { backStackEntry ->
                val libraryDetails: AboutScreens.LibraryDetails = backStackEntry.toRoute()
                val libs = libraries[LibraryPartyInfo from libraryDetails.partyId]!!
                val library = libs.find { it.uniqueId == libraryDetails.libraryId }!!

                LibraryScreen(
                    animatedContentScope = this,
                    backOnClick = { navController.navigateUp() },
                    library = library,
                    colors = libraryColors,
                    padding = libraryPadding,
                    extras = libraryExtras,
                    textStyles = libraryTextStyles,
                )
            }
        }
    }
}