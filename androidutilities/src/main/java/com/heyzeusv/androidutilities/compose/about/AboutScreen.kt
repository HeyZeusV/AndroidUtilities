package com.heyzeusv.androidutilities.compose.about

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.heyzeusv.androidutilities.R
import com.heyzeusv.androidutilities.compose.about.overview.AboutOverview
import com.heyzeusv.androidutilities.compose.about.library.LibraryColors
import com.heyzeusv.androidutilities.compose.about.library.LibraryDefaults
import com.heyzeusv.androidutilities.compose.about.library.LibraryExtras
import com.heyzeusv.androidutilities.compose.about.library.LibraryPadding
import com.heyzeusv.androidutilities.compose.about.library.LibraryGroup
import com.heyzeusv.androidutilities.compose.about.library.AboutLibrary
import com.heyzeusv.androidutilities.compose.about.library.LibraryTextStyles
import com.heyzeusv.androidutilities.compose.about.overview.InfoEntry
import com.heyzeusv.androidutilities.compose.about.overview.OverviewColors
import com.heyzeusv.androidutilities.compose.about.overview.OverviewDefaults
import com.heyzeusv.androidutilities.compose.about.overview.OverviewExtras
import com.heyzeusv.androidutilities.compose.about.overview.OverviewPadding
import com.heyzeusv.androidutilities.compose.about.overview.OverviewTextStyles
import com.heyzeusv.androidutilities.compose.util.pRes
import com.mikepenz.aboutlibraries.entity.Library
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun AboutScreen(
    backButton: @Composable () -> Unit = { },
    icon: @Composable (BoxScope.() -> Unit)? = null,
    title: String,
    version: String,
    infoList: ImmutableList<InfoEntry>,
    separateByParty: Boolean = true,
    overviewColors: OverviewColors = OverviewDefaults.overviewColors(),
    overviewPadding: OverviewPadding = OverviewDefaults.overviewPadding(),
    overviewExtras: OverviewExtras = OverviewDefaults.overviewExtras(),
    overviewTextStyles: OverviewTextStyles = OverviewDefaults.overviewTextStyles(),
    libraryColors: LibraryColors = LibraryDefaults.libraryColors(),
    libraryPadding: LibraryPadding =
        LibraryDefaults.libraryPadding(outerPadding = LibraryDefaults.ScreenOuterPV),
    libraryExtras: LibraryExtras =
        LibraryDefaults.libraryExtras(actionIcon = pRes(R.drawable.icon_collapse)),
    libraryTextStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
) {
    val libraries by produceLibraryListState(separateByParty = separateByParty)

    AboutScreen(
        backButton = backButton,
        icon = icon,
        title = title,
        version = version,
        infoList = infoList,
        libraries = libraries,
        overviewColors = overviewColors,
        overviewPadding = overviewPadding,
        overviewExtras = overviewExtras,
        overviewTextStyles = overviewTextStyles,
        libraryColors = libraryColors,
        libraryPadding = libraryPadding,
        libraryExtras = libraryExtras,
        libraryTextStyles = libraryTextStyles,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AboutScreen(
    backButton: @Composable () -> Unit = { },
    icon: @Composable (BoxScope.() -> Unit)? = null,
    title: String,
    version: String,
    infoList: ImmutableList<InfoEntry>,
    libraries: ImmutableMap<LibraryGroup, ImmutableList<Library>>,
    overviewColors: OverviewColors = OverviewDefaults.overviewColors(),
    overviewPadding: OverviewPadding = OverviewDefaults.overviewPadding(),
    overviewExtras: OverviewExtras = OverviewDefaults.overviewExtras(),
    overviewTextStyles: OverviewTextStyles = OverviewDefaults.overviewTextStyles(),
    libraryColors: LibraryColors = LibraryDefaults.libraryColors(),
    libraryPadding: LibraryPadding =
        LibraryDefaults.libraryPadding(outerPadding = LibraryDefaults.ScreenOuterPV),
    libraryExtras: LibraryExtras =
        LibraryDefaults.libraryExtras(actionIcon = pRes(R.drawable.icon_collapse)),
    libraryTextStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
) {
    val navController = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = AboutScreens.Overview
        ) {
            composable<AboutScreens.Overview> {
                AboutOverview(
                    animatedContentScope = this,
                    backButton = backButton,
                    icon = icon,
                    title = title,
                    version = version,
                    infoList = infoList,
                    libraries = libraries,
                    libraryOnClick = { libraryGroup, libraryId ->
                        navController.navigate(AboutScreens.Library(libraryGroup, libraryId))
                    },
                    colors = overviewColors,
                    padding = overviewPadding,
                    extras = overviewExtras,
                    textStyles = overviewTextStyles,
                )
            }
            composable<AboutScreens.Library> { backStackEntry ->
                val libraryDetails: AboutScreens.Library = backStackEntry.toRoute()
                val libs = libraries[libraryDetails.libraryGroup]!!
                val library = libs.find { it.uniqueId == libraryDetails.libraryId }!!

                AboutLibrary(
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