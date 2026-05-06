package com.songlink.share

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.songlink.share.ui.ShareScreen
import com.songlink.share.ui.theme.SonglinkShareTheme

/**
 * The single activity of Songlink Share.
 *
 * This activity is declared in [AndroidManifest.xml] with two intent filters:
 *   1. MAIN / LAUNCHER — standard app launcher entry point.
 *   2. ACTION_SEND + text/plain — share sheet entry point used by music apps.
 *
 * When launched via the share sheet, [intent] carries the shared text in
 * [android.content.Intent.EXTRA_TEXT]. We pass the intent directly to
 * [ShareViewModel.handleIntent], which extracts the first URL, calls the
 * Songlink API, and exposes the result as a [kotlinx.coroutines.flow.StateFlow].
 *
 * [onNewIntent] handles the case where the activity is already in the back
 * stack (launchMode="singleTop") when a second share arrives — without it,
 * the old result would persist on screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val viewModel: ShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Process the share intent that launched this activity.
        // If the activity was restored from the back stack with a savedInstanceState
        // (e.g. after a config change), the ViewModel already holds the previous
        // result so we skip re-processing the intent.
        if (savedInstanceState == null) {
            viewModel.handleIntent(intent, applicationContext)
        }

        setContent {
            SonglinkShareTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Songlink Share",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors()
                        )
                    }
                ) { innerPadding ->
                    val state by viewModel.state.collectAsState()

                    ShareScreen(
                        state = state,
                        onCopyAgain = { pageUrl ->
                            viewModel.copyToClipboard(applicationContext, pageUrl)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    /**
     * Called when a new [Intent.ACTION_SEND] arrives while the activity is
     * already running (because launchMode="singleTop" prevents re-creation).
     *
     * Without this override, sharing a second song while the app is visible
     * would show the result from the first share.
     */
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIntent(intent, applicationContext)
    }
}
