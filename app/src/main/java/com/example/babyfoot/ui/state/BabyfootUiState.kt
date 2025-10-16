package com.example.babyfoot.ui.state

import com.example.babyfoot.model.Player

data class BabyfootUiState(
    val isLoading: Boolean = false,
    val players: List<Player> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)
