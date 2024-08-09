package com.heyzeusv.androidutilities.compose.util

import androidx.annotation.ArrayRes
import androidx.annotation.BoolRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp

/**
 *  Composable resource functions with shorten names.
 *  I highly recommend pairing these with Live Templates and having "Add unambiguous imports on the
 *  fly" turned on. Setting is found File -> Settings -> Editor -> General -> Auto Import.
 *
 *  ComposeResources xml and zip files can be found under /livetemplates folder. If you know
 *  location of your Android Studio configuration directory, you can copy ComposeResources.xml
 *  directly into your templates folder. Else you can import ComposeResources.zip by File ->
 *  Manage IDE Settings -> Import Settings... (ComposeResources.zip does not touch any settings,
 *  only adds live templates)
 */
/**
 *  Load a string resource with formatting.
 *
 *  @param id The resource identifier.
 *  @param args The format arguments.
 *  @return The string data associated with the resource.
 */
@Composable
@ReadOnlyComposable
fun sRes(@StringRes id: Int, vararg args: Any): String = stringResource(id, *args)

/**
 *  Load a string array resource.
 *
 *  @param id The resource identifier.
 *  @return The string array data associated with the resource.
 */
@Composable
@ReadOnlyComposable
fun saRes(@ArrayRes id: Int): Array<String> = stringArrayResource(id)

/**
 * Load a plurals resource with provided format arguments.
 *
 *  @param id the resource identifier
 *  @param count the count
 *  @param args arguments used in the format string
 *  @return the pluralized string data associated with the resource
 */
@Composable
@ReadOnlyComposable
fun psRes(@PluralsRes id: Int, count: Int, vararg args: Any): String =
    pluralStringResource(id, count, *args)

/**
 *  Create a [Painter] from an Android resource id.
 *
 *  @param id Resources object to query the image file from.
 *  @return [Painter] used for drawing the loaded resource.
 */
@Composable
fun pRes(@DrawableRes id: Int): Painter = painterResource(id)

/**
 *  Load a dimension resource.
 *
 *  @param id The resource identifier.
 *  @return The dimension value associated with the resource.
 */
@Composable
@ReadOnlyComposable
fun dRes(@DimenRes id: Int): Dp = dimensionResource(id)

/**
 *  Load a boolean resource.
 *
 *  @param id the resource identifier
 *  @return the boolean associated with the resource
 */
@Composable
@ReadOnlyComposable
fun bRes(@BoolRes id: Int): Boolean = booleanResource(id)

/**
 *  Load a integer resource.
 *
 *  @param id The resource identifier.
 *  @return The integer value associated with the resource.
 */
@Composable
@ReadOnlyComposable
fun iRes(@IntegerRes id: Int): Int = integerResource(id)

/**
 *  Load an array of integer resource.
 *
 *  @param id the resource identifier
 *  @return the integer array associated with the resource
 */
@Composable
@ReadOnlyComposable
fun iaRes(@ArrayRes id: Int): IntArray = integerArrayResource(id)

/**
 *  Load a color resource.
 *
 *  @param id the resource identifier
 *  @return the color associated with the resource
 */
@Composable
@ReadOnlyComposable
fun cRes(@ColorRes id: Int): Color = colorResource(id)

/**
 *  Load an [AnimatedImageVector] from an Android resource id.
 *
 *  @param id the resource identifier
 *  @return an animated vector drawable resource.
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun avRes(@DrawableRes id: Int): AnimatedImageVector = AnimatedImageVector.animatedVectorResource(id)