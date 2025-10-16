package com.example.babyfoot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.babyfoot.data.FirebaseRealtimeRepository
import com.example.babyfoot.model.MatchSubmission
import com.example.babyfoot.ui.state.BabyfootUiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BabyfootViewModel(
    private val repository: FirebaseRealtimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BabyfootUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshPlayers()
    }

    fun refreshPlayers(message: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = message) }
            runCatching { repository.getPlayers() }
                .onSuccess { players ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            players = players,
                            errorMessage = null,
                            infoMessage = message
                        )
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Echec de la lecture de la base Firebase."
                        )
                    }
                }
        }
    }

    fun addPlayer(name: String) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Le nom du joueur est requis.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.addPlayer(name.trim()) }
                .onSuccess { players ->
                    _uiState.update {
                        it.copy(
                            players = players,
                            isLoading = false,
                            infoMessage = "$name rejoint la ligue !"
                        )
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Impossible d'ajouter ce joueur."
                        )
                    }
                }
        }
    }

    fun recordMatch(submission: MatchSubmission) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.recordMatch(submission) }
                .onSuccess { players ->
                    _uiState.update {
                        it.copy(
                            players = players,
                            isLoading = false,
                            infoMessage = "Match enregistre, classement mis a jour."
                        )
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message
                                ?: "Impossible d'enregistrer ce match."
                        )
                    }
                }
        }
    }

    fun clearTransientMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    class Factory(
        private val repository: FirebaseRealtimeRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BabyfootViewModel::class.java)) {
                return BabyfootViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
