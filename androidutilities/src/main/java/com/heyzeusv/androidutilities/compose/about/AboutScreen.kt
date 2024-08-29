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

/**
 *  Full screen Composable that contains a NavGraph that contains 2 screens. Transition between
 *  screens is done using Shared Element Transitions.
 *
 *  [AboutOverview] displays app info and list of libraries.
 *
 *  [AboutLibrary] displays full library information including its description and full license.
 *
 *  This version gives the option to either list all libraries under one header or separate by
 *  first party (Google, Jetbrains, Kotlin) and third party libraries using [separateByParty].
 *
 *  @param backButton Allows for back button if not using TopAppBar navigation.
 *  @param icon Optional icon displayed above [title] Text.
 *  @param title Title for screen.
 *  @param version Text meant to display current version of app.
 *  @param infoList String or String Resource, along with its styling, to be displayed in
 *  HorizontalPager.
 *  @param separateByParty Used to decide whether to list all libraries under single header or to
 *  separate by first and third party.
 *  @param overviewColors Colors to be used on [AboutOverview].
 *  @param overviewPadding Padding to be used on [AboutOverview].
 *  @param overviewExtras Additional values to be used on [AboutOverview].
 *  @param overviewTextStyles Text Styles to be used on [AboutOverview].
 *  @param libraryColors Colors to be used on [AboutLibrary].
 *  @param libraryPadding Padding to be used on [AboutLibrary].
 *  @param libraryExtras Additional values to be used on [AboutLibrary].
 *  @param libraryTextStyles Text Styles to be used on [AboutLibrary].
 */
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

/**
 *  Full screen Composable that contains a NavGraph that contains 2 screens. Transition between
 *  screens is done using Shared Element Transitions.
 *
 *  [AboutOverview] displays app info and list of libraries.
 *
 *  [AboutLibrary] displays full library information including its description and full license.
 *
 *  This version allows for custom listing of libraries by passing [libraries].
 *
 *  @param backButton Allows for back button if not using TopAppBar navigation.
 *  @param icon Optional icon displayed above [title] Text.
 *  @param title Title for screen.
 *  @param version Text meant to display current version of app.
 *  @param infoList String or String Resource, along with its styling, to be displayed in
 *  HorizontalPager.
 *  @param libraries Custom map of libraries where each [LibraryGroup] is paired to the list of
 *  [Library] to be displayed under it.
 *  @param overviewColors Colors to be used on [AboutOverview].
 *  @param overviewPadding Padding to be used on [AboutOverview].
 *  @param overviewExtras Additional values to be used on [AboutOverview].
 *  @param overviewTextStyles Text Styles to be used on [AboutOverview].
 *  @param libraryColors Colors to be used on [AboutLibrary].
 *  @param libraryPadding Padding to be used on [AboutLibrary].
 *  @param libraryExtras Additional values to be used on [AboutLibrary].
 *  @param libraryTextStyles Text Styles to be used on [AboutLibrary].
 */
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