package com.oapps.chessknights.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.oapps.chessknights.R

val SourceSansProFontFamily = FontFamily(
    Font(R.font.source_sans_pro_bold_italic, FontWeight(700), FontStyle.Italic),
    Font(R.font.source_sans_pro_semibold, FontWeight(600))
)

// Set of Material typography styles to start with
val Typography = Typography(
    defaultFontFamily = SourceSansProFontFamily,
    body1 = TextStyle(
        fontFamily = SourceSansProFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)