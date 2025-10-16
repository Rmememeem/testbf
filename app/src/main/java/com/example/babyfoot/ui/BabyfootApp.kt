package com.example.babyfoot.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.babyfoot.model.MatchSubmission
import com.example.babyfoot.ui.navigation.AppDestination
import com.example.babyfoot.ui.navigation.destinations
import com.example.babyfoot.ui.screens.HomeScreen
import com.example.babyfoot.ui.screens.MatchScreen
import com.example.babyfoot.ui.screens.RankingScreen
import com.example.babyfoot.ui.state.BabyfootUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyfootApp(
    uiState: BabyfootUiState,
    snackbarHostState: SnackbarHostState,
    onAddPlayer: (String) -> Unit,
    onRefresh: () -> Unit,
    onMatchValidated: (MatchSubmission) -> Unit,
    onMessagesConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    var selectedDestination by remember { mutableStateOf(AppDestination.Home) }

    LaunchedEffect(uiState.errorMessage, uiState.infoMessage) {
        when {
            uiState.errorMessage != null -> {
                snackbarHostState.showSnackbar(uiState.errorMessage)
                onMessagesConsumed()
            }
            uiState.infoMessage != null -> {
                snackbarHostState.showSnackbar(uiState.infoMessage)
                onMessagesConsumed()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = selectedDestination.title) },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Rafraichir")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination,
                        onClick = {
                            selectedDestination = destination
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.title) },
                        label = { Text(destination.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = AppDestination.Home.route
            ) {
                composable(AppDestination.Home.route) {
                    HomeScreen(
                        uiState = uiState,
                        onAddPlayer = onAddPlayer,
                        onMatchClicked = {
                            selectedDestination = AppDestination.Match
                            navController.navigate(AppDestination.Match.route)
                        },
                        onRankingClicked = {
                            selectedDestination = AppDestination.Ranking
                            navController.navigate(AppDestination.Ranking.route)
                        }
                    )
                }
                composable(AppDestination.Ranking.route) {
                    RankingScreen(players = uiState.players)
                }
                composable(AppDestination.Match.route) {
                    MatchScreen(
                        players = uiState.players,
                        isLoading = uiState.isLoading,
                        onSubmit = onMatchValidated
                    )
                }
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                )
            }
        }
    }
}
