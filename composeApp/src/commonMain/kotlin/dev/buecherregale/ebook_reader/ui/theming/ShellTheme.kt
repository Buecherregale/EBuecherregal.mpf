package dev.buecherregale.ebook_reader.ui.theming

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ShellTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) ShellDarkColorScheme else ShellLightColorScheme,
        content = content
    )
}
