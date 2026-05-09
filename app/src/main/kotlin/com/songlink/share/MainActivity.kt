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

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val viewModel: ShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            viewModel.handleIntent(intent, applicationContext)
        }

        setContent {
            SonglinkShareTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("Songlink Share", fontWeight = FontWeight.SemiBold) },
                            colors = TopAppBarDefaults.topAppBarColors()
                        )
                    }
                ) { innerPadding ->
                    val state by viewModel.state.collectAsState()
                    val history by viewModel.history.collectAsState()

                    ShareScreen(
                        state = state,
                        history = history,
                        onCopyAgain = { viewModel.copyToClipboard(applicationContext, it) },
                        onCopyHistory = { viewModel.copyToClipboard(applicationContext, it) },
                        onClearHistory = { viewModel.clearHistory(applicationContext) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIntent(intent, applicationContext)
    }
}
