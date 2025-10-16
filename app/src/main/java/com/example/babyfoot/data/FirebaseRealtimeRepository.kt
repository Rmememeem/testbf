package com.example.babyfoot.data

import com.example.babyfoot.logic.EloCalculator
import com.example.babyfoot.model.MatchSubmission
import com.example.babyfoot.model.MatchType
import com.example.babyfoot.model.Player
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

class FirebaseRealtimeRepository(
    private val baseUrl: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getPlayers(): List<Player> = withContext(dispatcher) {
        loadPlayers().values.sortedByDescending { it.elo }
    }

    suspend fun addPlayer(name: String): List<Player> = withContext(dispatcher) {
        if (name.isBlank()) {
            throw IllegalArgumentException("Le nom du joueur est requis.")
        }
        val players = loadPlayers().toMutableMap()
        if (players.containsKey(name)) {
            throw IllegalArgumentException("Ce joueur existe deja.")
        }
        players[name] = Player(name, DEFAULT_ELO, 0, 0)
        persistPlayers(players.values)
        players.values.sortedByDescending { it.elo }
    }

    suspend fun recordMatch(submission: MatchSubmission): List<Player> = withContext(dispatcher) {
        val players = loadPlayers().toMutableMap()

        val participants = submission.winners + submission.losers
        if (participants.size != participants.toSet().size) {
            throw IllegalArgumentException("Tous les joueurs doivent etre distincts.")
        }
        val missing = participants.filterNot { players.containsKey(it) }
        if (missing.isNotEmpty()) {
            throw IllegalArgumentException("Joueurs introuvables: ${missing.joinToString()}")
        }

        when (submission.matchType) {
            MatchType.SOLO -> updateSolo(players, submission)
            MatchType.DUO -> updateDuo(players, submission)
        }

        persistPlayers(players.values)
        players.values.sortedByDescending { it.elo }
    }

    private fun updateSolo(
        players: MutableMap<String, Player>,
        submission: MatchSubmission
    ) {
        val winner = players.getValue(submission.winners.first())
        val loser = players.getValue(submission.losers.first())
        val (winnerElo, loserElo) = EloCalculator.update(
            ratingA = winner.elo,
            ratingB = loser.elo,
            scoreA = 1.0
        )
        players[winner.name] = winner.copy(
            elo = winnerElo,
            wins = winner.wins + 1
        )
        players[loser.name] = loser.copy(
            elo = loserElo,
            losses = loser.losses + 1
        )
    }

    private fun updateDuo(
        players: MutableMap<String, Player>,
        submission: MatchSubmission
    ) {
        val winners = submission.winners.map { players.getValue(it) }
        val losers = submission.losers.map { players.getValue(it) }
        require(winners.size == 2 && losers.size == 2) { "Les matchs 2v2 necessitent 4 joueurs distincts." }
        val (deltaW, deltaL) = EloCalculator.teamAdjustments(
            winners = winners[0].elo to winners[1].elo,
            losers = losers[0].elo to losers[1].elo
        )

        winners.forEach { player ->
            players[player.name] = player.copy(
                elo = (player.elo + deltaW).roundToInt(),
                wins = player.wins + 1
            )
        }
        losers.forEach { player ->
            players[player.name] = player.copy(
                elo = (player.elo + deltaL).roundToInt(),
                losses = player.losses + 1
            )
        }
    }

    private fun loadPlayers(): Map<String, Player> {
        val connection = openConnection("players.json")
        connection.requestMethod = "GET"
        return connection.consumeResponse { payload ->
            if (payload.isBlank() || payload == "null") {
                emptyMap()
            } else {
                val json = JSONObject(payload)
                val result = mutableMapOf<String, Player>()
                json.keys().forEach { key ->
                    val playerJson = json.optJSONObject(key) ?: return@forEach
                    val elo = playerJson.optInt("elo", DEFAULT_ELO)
                    val wins = playerJson.optInt("wins", 0)
                    val losses = playerJson.optInt("losses", 0)
                    result[key] = Player(key, elo, wins, losses)
                }
                result
            }
        }
    }

    private fun persistPlayers(players: Collection<Player>) {
        val json = JSONObject()
        players.forEach { player ->
            json.put(
                player.name,
                JSONObject().apply {
                    put("elo", player.elo)
                    put("wins", player.wins)
                    put("losses", player.losses)
                }
            )
        }

        val connection = openConnection("players.json")
        connection.requestMethod = "PUT"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.bufferedWriter().use { writer ->
            writer.write(json.toString())
        }
        connection.ensureSuccess()
    }

    private fun openConnection(path: String): HttpURLConnection {
        val normalizedBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val url = URL("$normalizedBase$path")
        return (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
        }
    }

    private inline fun <T> HttpURLConnection.consumeResponse(block: (String) -> T): T {
        try {
            val responseBody = if (responseCode in 200..299) {
                inputStream.bufferedReader().use { it.readText() }
            } else {
                val errorDetails = errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IOException("Firebase error $responseCode: $errorDetails")
            }
            return block(responseBody)
        } finally {
            disconnect()
        }
    }

    private fun HttpURLConnection.ensureSuccess() {
        try {
            if (responseCode !in 200..299) {
                val errorDetails = errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IOException("Firebase error $responseCode: $errorDetails")
            } else {
                inputStream.close()
            }
        } finally {
            disconnect()
        }
    }

    companion object {
        private const val DEFAULT_ELO = 1000
        private const val TIMEOUT_MS = 5000
    }
}
