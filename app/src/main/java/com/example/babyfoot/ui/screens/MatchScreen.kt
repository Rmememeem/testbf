package com.example.babyfoot.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.SportsKabaddi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.babyfoot.model.MatchSubmission
import com.example.babyfoot.model.MatchType
import com.example.babyfoot.model.Player

@Composable
fun MatchScreen(
    players: List<Player>,
    isLoading: Boolean,
    onSubmit: (MatchSubmission) -> Unit
) {
    Surface {
        when {
            players.size < 2 -> EmptyState(message = "Ajoute au moins deux joueurs pour enregistrer un match.")
            else -> MatchForm(players = players, isLoading = isLoading, onSubmit = onSubmit)
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MatchForm(
    players: List<Player>,
    isLoading: Boolean,
    onSubmit: (MatchSubmission) -> Unit
) {
    val names = remember(players) { players.map(Player::name) }
    var matchType by remember { mutableStateOf(MatchType.SOLO) }

    var winnerOne by remember { mutableStateOf("") }
    var winnerTwo by remember { mutableStateOf("") }
    var loserOne by remember { mutableStateOf("") }
    var loserTwo by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Type de match",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MatchTypeChip(
                        label = "1 vs 1",
                        selected = matchType == MatchType.SOLO,
                        onClick = {
                            matchType = MatchType.SOLO
                            winnerTwo = ""
                            loserTwo = ""
                        }
                    )
                    MatchTypeChip(
                        label = "2 vs 2",
                        selected = matchType == MatchType.DUO,
                        onClick = { matchType = MatchType.DUO }
                    )
                }
            }
        }
        Card(
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Equipe 1",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                PlayerDropdown(
                    label = "Gagnant #1",
                    value = winnerOne,
                    options = names,
                    onValueChange = { winnerOne = it }
                )
                if (matchType == MatchType.DUO) {
                    PlayerDropdown(
                        label = "Gagnant #2",
                        value = winnerTwo,
                        options = names,
                        onValueChange = { winnerTwo = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Equipe 2",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                PlayerDropdown(
                    label = "Perdant #1",
                    value = loserOne,
                    options = names,
                    onValueChange = { loserOne = it }
                )
                if (matchType == MatchType.DUO) {
                    PlayerDropdown(
                        label = "Perdant #2",
                        value = loserTwo,
                        options = names,
                        onValueChange = { loserTwo = it }
                    )
                }
            }
        }

        Button(
            onClick = {
                val winners = buildList {
                    add(winnerOne)
                    if (matchType == MatchType.DUO) add(winnerTwo)
                }.filter { it.isNotBlank() }

                val losers = buildList {
                    add(loserOne)
                    if (matchType == MatchType.DUO) add(loserTwo)
                }.filter { it.isNotBlank() }

                if (winners.isEmpty() || losers.isEmpty()) {
                    return@Button
                }
                if (winners.toSet().size != winners.size || losers.toSet().size != losers.size) {
                    return@Button
                }
                val allPlayers = (winners + losers)
                if (allPlayers.toSet().size != allPlayers.size) {
                    return@Button
                }

                val submission = MatchSubmission(
                    matchType = matchType,
                    winners = winners,
                    losers = losers
                )
                onSubmit(submission)
            },
            enabled = !isLoading && isFormValid(
                matchType = matchType,
                winnerOne = winnerOne,
                winnerTwo = winnerTwo,
                loserOne = loserOne,
                loserTwo = loserTwo
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enregistrer le match")
        }
    }
}

@Composable
private fun MatchTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(Icons.Rounded.SportsKabaddi, contentDescription = null)
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            labelColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            leadingIconContentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun PlayerDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasOptions = options.isNotEmpty()

    Column {
        androidx.compose.foundation.layout.Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                label = { Text(label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = hasOptions,
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { expanded = true },
                readOnly = true,
                enabled = hasOptions,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = null
                    )
                }
            )
            DropdownMenu(
                expanded = expanded && hasOptions,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
        if (!hasOptions) {
            Text(
                text = "Aucun joueur disponible",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun isFormValid(
    matchType: MatchType,
    winnerOne: String,
    winnerTwo: String,
    loserOne: String,
    loserTwo: String
): Boolean {
    if (winnerOne.isBlank() || loserOne.isBlank()) return false
    return when (matchType) {
        MatchType.SOLO -> winnerOne != loserOne
        MatchType.DUO -> {
            if (winnerTwo.isBlank() || loserTwo.isBlank()) return false
            val participants = listOf(winnerOne, winnerTwo, loserOne, loserTwo)
            participants.toSet().size == participants.size
        }
    }
}



