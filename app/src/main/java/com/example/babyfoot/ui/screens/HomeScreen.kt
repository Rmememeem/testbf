package com.example.babyfoot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Sports
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.babyfoot.model.Player
import com.example.babyfoot.ui.components.PlayerCard
import com.example.babyfoot.ui.state.BabyfootUiState

@Composable
fun HomeScreen(
    uiState: BabyfootUiState,
    onAddPlayer: (String) -> Unit,
    onMatchClicked: () -> Unit,
    onRankingClicked: () -> Unit,
) {
    val textState = remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        WelcomeCard(
            playerCount = uiState.players.size,
            onMatchClicked = onMatchClicked,
            onRankingClicked = onRankingClicked
        )
        Spacer(modifier = Modifier.height(16.dp))
        AddPlayerCard(textState = textState, onAddPlayer = onAddPlayer)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Top joueurs",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.players.isEmpty()) {
            Text(
                text = "Ajoute ton premier joueur et lance un match !",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(uiState.players.sortedByDescending(Player::elo).take(5)) { index, player ->
                PlayerCard(player = player, rank = index + 1)
            }
        }
    }
}

@Composable
private fun WelcomeCard(
    playerCount: Int,
    onMatchClicked: () -> Unit,
    onRankingClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Babyfoot Arena",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Rejoins $playerCount passionnes, enregistre les matchs et laisse l'ELO faire le show.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            FilledTonalButton(onClick = onMatchClicked) {
                Icon(Icons.Rounded.Sports, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nouveau match")
            }
            Button(onClick = onRankingClicked) {
                Icon(Icons.Rounded.Leaderboard, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voir le classement")
            }
        }
    }
}

@Composable
private fun AddPlayerCard(
    textState: MutableState<String>,
    onAddPlayer: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ajouter un joueur",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = textState.value,
                onValueChange = { textState.value = it },
                label = { Text("Nom du joueur") },
                trailingIcon = {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    onAddPlayer(textState.value)
                    textState.value = ""
                },
                enabled = textState.value.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Ajouter")
            }
        }
    }
}



