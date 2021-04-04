package com.oapps.chessknights.ui.theme

import android.view.View
import android.view.Window
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

private val DarkColorPalette = darkColors(
    primary = Blue200,
    primaryVariant = Blue700,
    secondary = Teal200
)

private val LightColorPalette = lightColors(
    primary = Blue500,
    primaryVariant = Blue700,
    secondary = Teal200

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

class ChessColors(
    surfaceWhite: Color,
    surfaceBlack: Color,
    tileBackgroundPieceSelectedLight: Color,
    tileBackgroundPieceSelectedDark: Color,
    tileBackgroundHighlightLight: Color,
    tileBackgroundHighlightDark: Color,
){
    fun copy() = ChessColors(
        surfaceWhite = surfaceLight,
        surfaceBlack = surfaceDark,
        tileBackgroundPieceSelectedLight = tileBackgroundPieceSelectedLight,
        tileBackgroundPieceSelectedDark = tileBackgroundPieceSelectedDark,
        tileBackgroundHighlightLight = tileBackgroundHighlightLight,
        tileBackgroundHighlightDark = tileBackgroundHighlightDark,
    )

    val tileBackgroundHighlightLight by mutableStateOf(tileBackgroundHighlightLight, structuralEqualityPolicy())
    val tileBackgroundHighlightDark by mutableStateOf(tileBackgroundHighlightDark, structuralEqualityPolicy())

    val tileBackgroundPieceSelectedLight by mutableStateOf(tileBackgroundPieceSelectedLight, structuralEqualityPolicy())
    val tileBackgroundPieceSelectedDark by mutableStateOf(tileBackgroundPieceSelectedDark, structuralEqualityPolicy())
    var surfaceLight by mutableStateOf(surfaceWhite, structuralEqualityPolicy())
    var surfaceDark by mutableStateOf(surfaceBlack, structuralEqualityPolicy())
}



val ChessLightColorPalette = ChessColors(
    surfaceWhite = Color(0xFFE8EAF6),
    surfaceBlack = Color(0xFF4859B9),
    tileBackgroundPieceSelectedLight = Color(0xFFFFEB3B),
    tileBackgroundPieceSelectedDark = Color(0xFFFFEB3B),
    tileBackgroundHighlightLight = Color(0xCCA4FF3B),
    tileBackgroundHighlightDark = Color(0xCCA4FF3B),
)

val LocalChessColor = staticCompositionLocalOf { ChessLightColorPalette }

@Composable
fun ChessKnightsTheme(
    windows: Window? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }


    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
    ){
        windows?.let {
            windows.statusBarColor = MaterialTheme.colors.surface.toArgb()
            windows.navigationBarColor = MaterialTheme.colors.surface.toArgb()

            @Suppress("DEPRECATION")
            if (MaterialTheme.colors.surface.luminance() > 0.5f) {
                windows.decorView.systemUiVisibility = windows.decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }else{
                windows.decorView.systemUiVisibility = windows.decorView.systemUiVisibility and
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }

            @Suppress("DEPRECATION")
            if (MaterialTheme.colors.surface.luminance() > 0.5f) {
                windows.decorView.systemUiVisibility = windows.decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }else{
                windows.decorView.systemUiVisibility = windows.decorView.systemUiVisibility and
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }
        content()
    }
}