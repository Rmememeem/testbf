package com.example.babyfoot.model

data class MatchSubmission(
    val matchType: MatchType,
    val winners: List<String>,
    val losers: List<String>
) {
    init {
        require(winners.isNotEmpty()) { "Winners cannot be empty" }
        require(losers.isNotEmpty()) { "Losers cannot be empty" }
    }
}
