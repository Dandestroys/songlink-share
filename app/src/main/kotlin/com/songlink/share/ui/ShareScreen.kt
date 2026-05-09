package com.songlink.share.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.songlink.share.R
import com.songlink.share.model.HistoryEntry
import com.songlink.share.model.SonglinkState
import com.songlink.share.ui.theme.SonglinkShareTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ShareScreen(
    state: SonglinkState,
    history: List<HistoryEntry>,
    onCopyAgain: (String) -> Unit,
    onCopyHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "state_transition"
        ) { currentState ->
            when (currentState) {
                is SonglinkState.Idle -> IdleScreen(
                    history = history,
                    onCopy = onCopyHistory,
                    onClearHistory = onClearHistory
                )
                is SonglinkState.Loading -> LoadingScreen()
                is SonglinkState.Success -> SuccessScreen(
                    pageUrl = currentState.pageUrl,
                    originalUrl = currentState.originalUrl,
                    onCopyAgain = { onCopyAgain(currentState.pageUrl) },
                    onOpenLink = { openUrl(context, currentState.pageUrl) }
                )
                is SonglinkState.Error -> ErrorScreen(message = currentState.message)
            }
        }
    }
}

@Composable
private fun IdleScreen(
    history: List<HistoryEntry>,
    onCopy: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    if (history.isEmpty()) {
        EmptyIdleScreen()
    } else {
        HistoryScreen(history = history, onCopy = onCopy, onClearHistory = onClearHistory)
    }
}

@Composable
private fun EmptyIdleScreen() {
    CenteredColumn {
        Icon(
            painter = painterResource(R.drawable.ic_music_note),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Songlink Share",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Share a song from Spotify, YouTube Music, Apple Music, " +
                   "or any other music app, then choose Songlink Share.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        AttributionText()
    }
}

@Composable
private fun HistoryScreen(
    history: List<HistoryEntry>,
    onCopy: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent songs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onClearHistory) { Text("Clear all") }
            }
            Spacer(Modifier.height(8.dp))
        }

        items(history, key = { it.pageUrl + it.timestamp }) { entry ->
            HistoryItem(entry = entry, onCopy = onCopy)
            Spacer(Modifier.height(8.dp))
        }

        item {
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AttributionText()
            }
        }
    }
}

@Composable
private fun HistoryItem(entry: HistoryEntry, onCopy: (String) -> Unit) {
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.pageUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = {
                onCopy(entry.pageUrl)
                copied = true
                scope.launch { delay(1500); copied = false }
            }) {
                Icon(
                    painter = painterResource(
                        if (copied) R.drawable.ic_check_circle else R.drawable.ic_content_copy
                    ),
                    contentDescription = if (copied) "Copied" else "Copy",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    CenteredColumn {
        CircularProgressIndicator(modifier = Modifier.size(56.dp), strokeWidth = 4.dp)
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Fetching Songlink…",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Resolving across all music platforms",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuccessScreen(
    pageUrl: String,
    originalUrl: String,
    onCopyAgain: () -> Unit,
    onOpenLink: () -> Unit
) {
    var showCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CenteredColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
        Icon(
            painter = painterResource(R.drawable.ic_check_circle),
            contentDescription = "Success",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Copied to clipboard!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your Songlink smart-link is ready to paste anywhere.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_link),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = pageUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(
                onClick = {
                    onCopyAgain()
                    showCopied = true
                    scope.launch { delay(1500); showCopied = false }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(
                        if (showCopied) R.drawable.ic_check_circle else R.drawable.ic_content_copy
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(if (showCopied) "Copied!" else "Copy again")
            }
            Button(onClick = onOpenLink, modifier = Modifier.weight(1f)) {
                Icon(
                    painter = painterResource(R.drawable.ic_open_in_browser),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text("Open link")
            }
        }
        Spacer(Modifier.height(32.dp))
        AttributionText()
    }
}

@Composable
private fun ErrorScreen(message: String) {
    CenteredColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
        Icon(
            painter = painterResource(R.drawable.ic_error_outline),
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = {}) {
            Text("Try again by re-sharing the link")
        }
        Spacer(Modifier.height(32.dp))
        AttributionText()
    }
}

@Composable
private fun AttributionText() {
    Text(
        text = "Powered by Songlink",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CenteredColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) { content() }
    }
}

private fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000L -> "Just now"
        diff < 3_600_000L -> "${diff / 60_000}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

@Preview(showBackground = true, name = "Idle - empty")
@Composable
private fun PreviewIdle() {
    SonglinkShareTheme { ShareScreen(SonglinkState.Idle, emptyList(), {}, {}, {}) }
}

@Preview(showBackground = true, name = "Idle - with history")
@Composable
private fun PreviewIdleHistory() {
    val history = listOf(
        HistoryEntry("https://song.link/s/abc123", "https://open.spotify.com/track/abc123", System.currentTimeMillis() - 300_000),
        HistoryEntry("https://song.link/s/def456", "https://music.apple.com/track/def456", System.currentTimeMillis() - 86_400_000)
    )
    SonglinkShareTheme { ShareScreen(SonglinkState.Idle, history, {}, {}, {}) }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun PreviewLoading() {
    SonglinkShareTheme { ShareScreen(SonglinkState.Loading, emptyList(), {}, {}, {}) }
}

@Preview(showBackground = true, name = "Success")
@Composable
private fun PreviewSuccess() {
    SonglinkShareTheme {
        ShareScreen(
            state = SonglinkState.Success(
                pageUrl = "https://song.link/s/6rqhFgbbKwnb9MLmUQDhG6",
                originalUrl = "https://open.spotify.com/track/6rqhFgbbKwnb9MLmUQDhG6"
            ),
            history = emptyList(),
            onCopyAgain = {},
            onCopyHistory = {},
            onClearHistory = {}
        )
    }
}
