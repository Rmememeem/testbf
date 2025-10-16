package com.example.babyfoot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.babyfoot.BuildConfig
import com.example.babyfoot.data.FirebaseRealtimeRepository
import com.example.babyfoot.ui.BabyfootApp
import com.example.babyfoot.ui.BabyfootViewModel
import com.example.babyfoot.ui.theme.BabyfootTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        FirebaseRealtimeRepository(
            baseUrl = BuildConfig.FIREBASE_DB_URL.ifBlank {
                error("Configure FIREBASE_DB_URL in build.gradle to point to your Firebase RTDB endpoint.")
            }
        )
    }
    private val viewModel: BabyfootViewModel by viewModels {
        BabyfootViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BabyfootTheme {
                val uiState by viewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                BabyfootApp(
                    uiState = uiState,
                    snackbarHostState = snackbarHostState,
                    onAddPlayer = { name -> viewModel.addPlayer(name) },
                    onRefresh = { viewModel.refreshPlayers() },
                    onMatchValidated = { submission -> viewModel.recordMatch(submission) },
                    onMessagesConsumed = { viewModel.clearTransientMessages() }
                )
            }
        }
    }
}

