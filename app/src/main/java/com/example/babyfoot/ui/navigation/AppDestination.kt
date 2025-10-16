package com.example.babyfoot.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.SportsScore
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    Home("home", "Accueil", Icons.Rounded.Home),
    Ranking("ranking", "Classement", Icons.Rounded.Leaderboard),
    Match("match", "Match", Icons.Rounded.SportsScore)
}

val destinations = AppDestination.values().toList()
