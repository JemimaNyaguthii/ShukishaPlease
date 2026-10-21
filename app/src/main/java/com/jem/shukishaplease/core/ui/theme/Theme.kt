package com.jem.shukishaplease.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ShukishaNeoBrutalistColorScheme = lightColorScheme(
    primary = NeoBlack,
    onPrimary = PureWhite,
    primaryContainer = NeoYellow,
    onPrimaryContainer = NeoBlack,
    secondary = NeoTeal,
    onSecondary = PureWhite,
    secondaryContainer = NeoSkyBlue,
    onSecondaryContainer = NeoBlack,
    tertiary = NeoPink,
    onTertiary = NeoBlack,
    background = NeoCreamBackground,
    onBackground = NeoBlack,
    surface = PureWhite,
    onSurface = NeoBlack,
    surfaceVariant = PureWhite,
    onSurfaceVariant = NeoMutedGrey,
    outline = NeoBlack,
    error = NeoCoral,
    onError = PureWhite
)

@Composable
fun ShukishaPleaseTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ShukishaNeoBrutalistColorScheme,
        typography = Typography,
        content = content
    )
}
