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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.songlink.share.model.SonglinkState
import com.songlink.share.ui.theme.SonglinkShareTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Root composable for the Songlink Share screen.
 *
 * Delegates to one of three sub-composables based on [state]:
 * - [IdleScreen]   — app opened standalone (no share intent)
 * - [LoadingScreen] — API call in flight
 * - [SuccessScreen] — pageUrl retrieved and copied to clipboard
 * - [ErrorScreen]  — any failure state
 */
@Composable
fun ShareScreen(
    state: SonglinkState,
    onCopyAgain: (String) -> Unit,
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
                is SonglinkState.Idle -> IdleScreen()
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

// ---------------------------------------------------------------------------
// Idle state — app opened from launcher, no share intent received
// ---------------------------------------------------------------------------

@Composable
private fun IdleScreen() {
    CenteredColumn {
        Icon(
            imageVector = Icons.Default.MusicNote,
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

// ---------------------------------------------------------------------------
// Loading state — waiting for the Songlink API response
// ---------------------------------------------------------------------------

@Composable
private fun LoadingScreen() {
    CenteredColumn {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            strokeWidth = 4.dp
        )
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

// ---------------------------------------------------------------------------
// Success state — pageUrl retrieved and copied to clipboard
// ---------------------------------------------------------------------------

@Composable
private fun SuccessScreen(
    pageUrl: String,
    originalUrl: String,
    onCopyAgain: () -> Unit,
    onOpenLink: () -> Unit
) {
    // Tracks whether the "Copy again" button should show a brief checkmark
    var showCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CenteredColumn(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        // Success icon
        Icon(
            imageVector = Icons.Default.CheckCircle,
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

        // URL display card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
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

        // Action buttons — "Copy again" and "Open link"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // "Copy again" — triggers a brief visual confirmation
            FilledTonalButton(
                onClick = {
                    onCopyAgain()
                    showCopied = true
                    scope.launch {
                        delay(1500)
                        showCopied = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (showCopied) Icons.Default.CheckCircle
                                  else Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(if (showCopied) "Copied!" else "Copy again")
            }

            // "Open link" — opens the song.link page in the browser
            Button(
                onClick = onOpenLink,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
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

// ---------------------------------------------------------------------------
// Error state — something went wrong
// ---------------------------------------------------------------------------

@Composable
private fun ErrorScreen(message: String) {
    CenteredColumn(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
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
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = { /* handled by activity via intent retry */ }) {
            Text("Try again by re-sharing the link")
        }
        Spacer(Modifier.height(32.dp))
        AttributionText()
    }
}

// ---------------------------------------------------------------------------
// Shared helpers
// ---------------------------------------------------------------------------

/** Attribution text displayed at the bottom of every screen. */
@Composable
private fun AttributionText() {
    Text(
        text = "Powered by Songlink",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

/** A centered full-screen column layout reused across all state screens. */
@Composable
private fun CenteredColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

/** Opens [url] in the default browser. */
private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, name = "Idle")
@Composable
private fun PreviewIdle() {
    SonglinkShareTheme { ShareScreen(SonglinkState.Idle, onCopyAgain = {}) }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun PreviewLoading() {
    SonglinkShareTheme { ShareScreen(SonglinkState.Loading, onCopyAgain = {}) }
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
            onCopyAgain = {}
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun PreviewError() {
    SonglinkShareTheme {
        ShareScreen(
            state = SonglinkState.Error(
                "The URL wasn't recognised by Songlink. Make sure you're sharing a supported music link."
            ),
            onCopyAgain = {}
        )
    }
}
