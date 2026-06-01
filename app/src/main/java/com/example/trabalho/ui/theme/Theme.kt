package com.example.trabalho.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val TrabalhoDarkColorScheme = darkColorScheme(
    // Ação Principal do App (Verde)
    primary = SemanticConfirm,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color.White,

    // Ações Secundárias / Edição (Amarelo/Laranja)
    secondary = SemanticWarning,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF824000),
    onSecondaryContainer = Color.White,

    // Informações / Destaques (Azul)
    tertiary = SemanticInfo,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF0D47A1),
    onTertiaryContainer = Color.White,

    // Ações Críticas / Exclusão (Vermelho)
    error = SemanticError,
    onError = Color.White,
    errorContainer = Color(0xFF7F0000),
    onErrorContainer = Color.White,

    // Estrutura de Fundo
    background = DarkBackground,
    onBackground = TextPrimaryDark,

    surface = DarkSurface,
    onSurface = TextPrimaryDark,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark
)

@Composable
fun TrabalhoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TrabalhoDarkColorScheme,
        typography = Typography,
        content = content
    )
}