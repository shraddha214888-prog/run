package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val EmergencyDarkColorScheme =
  darkColorScheme(
    primary = EmergencyRedBright,
    onPrimary = Color.White,
    primaryContainer = EmergencyRedDark,
    onPrimaryContainer = Color.White,
    secondary = BeaconBlueBright,
    onSecondary = Color.White,
    secondaryContainer = BeaconBlueDark,
    onSecondaryContainer = Color.White,
    tertiary = TrafficGreenBright,
    onTertiary = Color.Black,
    background = DarkHighwayBackground,
    onBackground = TextWhitePrimary,
    surface = DarkSurfaceCard,
    onSurface = TextWhitePrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextMutedSecondary,
    outline = DarkSurfaceCardBorder,
    error = EmergencyRed,
    onError = Color.White
  )

private val EmergencyLightColorScheme =
  lightColorScheme(
    primary = EmergencyRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = BeaconBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8E2FF),
    onSecondaryContainer = Color(0xFF001A41),
    tertiary = TrafficGreen,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = EmergencyRed,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) EmergencyDarkColorScheme else EmergencyLightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
