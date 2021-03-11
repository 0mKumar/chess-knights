package com.oapps.chessknights.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
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
){
    fun copy() = ChessColors(
        surfaceWhite = surfaceWhite,
        surfaceBlack = surfaceBlack
    )

    var surfaceWhite by mutableStateOf(surfaceWhite, structuralEqualityPolicy())
    var surfaceBlack by mutableStateOf(surfaceBlack, structuralEqualityPolicy())
}



val ChessLightColorPalette = ChessColors(
    surfaceWhite = Blue50,
    surfaceBlack = Blue600
)

val LocalChessColor = staticCompositionLocalOf { ChessLightColorPalette }

@Composable
fun ChessKnightsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
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
        content = content
    )
}