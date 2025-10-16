package com.example.babyfoot.model

data class Player(
    val name: String,
    val elo: Int,
    val wins: Int,
    val losses: Int
) {
    val matches: Int = wins + losses
    val winRate: Double = if (matches == 0) 0.0 else wins.toDouble() / matches.toDouble()
}
