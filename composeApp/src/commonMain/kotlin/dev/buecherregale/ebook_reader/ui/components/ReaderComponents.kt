package dev.buecherregale.ebook_reader.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ebook_reader.composeapp.generated.resources.Res
import ebook_reader.composeapp.generated.resources.arrow_back_24px
import ebook_reader.composeapp.generated.resources.arrow_forward_24px
import ebook_reader.composeapp.generated.resources.settings_24px
import org.jetbrains.compose.resources.painterResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReaderTopBar(title: String, onBackClick: () -> Unit, onSettingsClick: () -> Unit) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(painterResource(Res.drawable.arrow_back_24px), contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(painterResource(Res.drawable.settings_24px), contentDescription = "Settings")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    )
}

@Composable
fun ReaderBottomControls(
    currentProgress: () -> Double,
    onNextChapter: () -> Unit = {},
    onPreviousChapter: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val progress = (currentProgress() * 100).toInt()
            Text(
                text = "$progress%",
                style = MaterialTheme.typography.bodySmall
            )

            // Progress Slider
            LinearProgressIndicator(
                progress = { currentProgress().toFloat() },
                modifier = Modifier.fillMaxWidth()
                    .padding(4.dp)
                    .defaultMinSize(minHeight = 8.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPreviousChapter) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_back_24px),
                        contentDescription = "Previous Chapter"
                    )
                }
                IconButton(onClick = onNextChapter) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_forward_24px),
                        contentDescription = "Next Chapter"
                    )
                }
            }
        }
    }
}